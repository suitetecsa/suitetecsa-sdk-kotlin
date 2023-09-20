package cu.suitetecsa.sdk.nauta.scraper

import cu.suitetecsa.sdk.util.ExceptionHandler
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import cu.suitetecsa.sdk.nauta.core.component6
import cu.suitetecsa.sdk.nauta.core.exceptions.LoadInfoException
import cu.suitetecsa.sdk.nauta.core.extensions.*
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.domain.model.ResultType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal class UserPortalScrapperImpl : UserPortalScraper {

    private val loadInfoExceptionHandler = ExceptionHandler.builder(LoadInfoException::class.java).build()

    override fun parseErrors(html: String, message: String, exceptionHandler: ExceptionHandler): ResultType<String> {
        val htmlParsed = Jsoup.parse(html)
        return try {
            htmlParsed.throwExceptionOnFailure(message, User, exceptionHandler)
            ResultType.Success(html)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Failure(e)
        }
    }

    override fun parseCsrfToken(html: String): String {
        val htmlParsed = Jsoup.parse(html)
        htmlParsed.throwExceptionOnFailure("Fail to parse csrf token", User, loadInfoExceptionHandler)
        return htmlParsed.selectFirst("input[name=csrf]")?.attr("value") ?: ""
    }

    override fun parseNautaUser(html: String, exceptionHandler: ExceptionHandler?): NautaUser {
        val htmlParsed = Jsoup.parse(html)
        htmlParsed.throwExceptionOnFailure(
            "Fail to parsing user information",
            User,
            exceptionHandler ?: loadInfoExceptionHandler
        )
        return parseUserAttributes(htmlParsed)
    }

    private fun parseUserAttributes(htmlParsed: Document): NautaUser {
        val attrs = htmlParsed.selectFirst(".z-depth-1")!!.select(".m6")
        val attrList = attrs.map { it.selectFirst("p")!!.text().trim() }
        return NautaUser(
            attrList[0], attrList[1], attrList[2], attrList[3], attrList[4], attrList[5], attrList[6], attrList[7],
            if (attrList.size > 8) attrList[8] else null, if (attrList.size > 9) attrList[9] else null,
            if (attrList.size > 10) attrList[10] else null, if (attrList.size > 11) attrList[11] else null,
            if (attrList.size > 12) attrList[12] else null, if (attrList.size > 13) attrList[13] else null,
            if (attrList.size > 14) attrList[14] else null, if (attrList.size > 15) attrList[15] else null,
            if (attrList.size > 16) attrList[16] else null, if (attrList.size > 17) attrList[17] else null,
            if (attrList.size > 18) attrList[18] else null, if (attrList.size > 19) attrList[19] else null,
            if (attrList.size > 20) attrList[20] else null
        )
    }

    override fun parseConnectionsSummary(html: String): ConnectionsSummary {
        val htmlParsed = Jsoup.parse(html)
        htmlParsed.throwExceptionOnFailure("Fail to get connections summary", User, loadInfoExceptionHandler)
        val (connections, totalTime, totalImport, uploader, downloader, totalTraffic) =
            htmlParsed.selectFirst("#content")!!.select(".card-content")
        return ConnectionsSummary(
            connections.selectFirst("input[name=count]")!!.attr("value").toInt(),
            connections.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            totalTime.selectFirst(".card-stats-number")!!.text().trim().toSeconds(),
            totalImport.selectFirst(".card-stats-number")!!.text().trim().toPriceFloat(),
            uploader.selectFirst(".card-stats-number")!!.text().trim().toBytes(),
            downloader.selectFirst(".card-stats-number")!!.text().trim().toBytes(),
            totalTraffic.selectFirst(".card-stats-number")!!.text().trim().toBytes()
        )
    }

    private fun parseSummary(html: String): Triple<Int, String, Float> {
        val parsedHtml = Jsoup.parse(html)
        parsedHtml.throwExceptionOnFailure("Fail to get summary", User, loadInfoExceptionHandler)
        val (data, totalImport) = parsedHtml.selectFirst("#content")!!.select(".card-content")
        return Triple(
            data.selectFirst("input[name=count]")?.attr("value")?.toIntOrNull() ?: 0,
            data.selectFirst("input[name=year_month_selected]")?.attr("value") ?: "",
            totalImport.selectFirst(".card-stats-number")?.text()?.trim()?.toPriceFloat() ?: 0f
        )
    }

    override fun parseRechargesSummary(html: String): RechargesSummary {
        val (count, yearMonthSelected, totalImport) = parseSummary(html)
        return RechargesSummary(count, yearMonthSelected, totalImport)
    }

    override fun parseTransfersSummary(html: String): TransfersSummary {
        val (count, yearMonthSelected, totalImport) = parseSummary(html)
        return TransfersSummary(count, yearMonthSelected, totalImport)
    }

    override fun parseQuotesPaidSummary(html: String): QuotesPaidSummary {
        val (count, yearMonthSelected, totalImport) = parseSummary(html)
        return QuotesPaidSummary(count, yearMonthSelected, totalImport)
    }

    private fun <T> parseList(html: String, parser: (Element) -> List<Element>, constructor: (Element) -> T): List<T> {
        val list = mutableListOf<T>()
        val parsedHtml = Jsoup.parse(html)
        parsedHtml.throwExceptionOnFailure("Fail to get action list", User, loadInfoExceptionHandler)
        val tableBody = parsedHtml.selectFirst(".responsive-table > tbody")
        tableBody?.let { body ->
            parser(body).let { rows -> list.addAll(rows.map { constructor(it) }) }
        }
        return list
    }

    override fun parseConnections(html: String): List<Connection> =
        parseList(html, { it.select("tr") }, ::parseConnection)

    override fun parseRecharges(html: String): List<Recharge> =
        parseList(html, { it.select("tr") }, ::parseRecharge)

    override fun parseTransfers(html: String): List<Transfer> =
        parseList(html, { it.select("tr") }, ::parseTransfer)

    override fun parseQuotesPaid(html: String): List<QuotePaid> =
        parseList(html, { it.select("tr") }, ::parseQuotePaid)

    private fun parseConnection(element: Element): Connection {
        val (startSessionTag, endSessionTag, durationTag, uploadedTag, downloadedTag, importTag) = element.select("td")
        return Connection(
            startSessionTag.text().trim().toDateTime(),
            endSessionTag.text().trim().toDateTime(),
            durationTag.text().trim().toSeconds(),
            uploadedTag.text().trim().toBytes(),
            downloadedTag.text().trim().toBytes(),
            importTag.text().trim().toPriceFloat()
        )
    }

    private fun parseRecharge(element: Element): Recharge {
        val (dateTag, importTag, channelTag, typeTag) = element.select("td")
        return Recharge(
            dateTag.text().trim().toDateTime(),
            importTag.text().trim().toPriceFloat(),
            channelTag.text().trim(),
            typeTag.text().trim()
        )
    }

    private fun parseTransfer(element: Element): Transfer {
        val (dateTag, importTag, destinyAccountTag) = element.select("td")
        return Transfer(
            dateTag.text().trim().toDateTime(),
            importTag.text().trim().toPriceFloat(),
            destinyAccountTag.text().trim()
        )
    }

    private fun parseQuotePaid(element: Element): QuotePaid {
        val (dateTag, importTag, channelTag, typeTag, officeTag) = element.select("td")
        return QuotePaid(
            dateTag.text().trim().toDateTime(),
            importTag.text().trim().toPriceFloat(),
            channelTag.text().trim(),
            typeTag.text().trim(),
            officeTag.text().trim()
        )
    }
}