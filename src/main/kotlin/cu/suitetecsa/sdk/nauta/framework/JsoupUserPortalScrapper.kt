package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.nauta.core.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.core.userPortal
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.domain.model.*
import org.jsoup.Jsoup

class JsoupUserPortalScrapper : UserPortalScraper {
    override fun parseErrors(html: String): ResultType<String> {
        val htmlParsed = Jsoup.parse(html)
        return try {
            htmlParsed.throwExceptionOnFailure(Exception::class.java, "nothing", userPortal)
            ResultType.Success(html)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }

    override fun parseCsrfToken(html: String): String {
        val htmlParsed = Jsoup.parse(html)
        return htmlParsed.selectFirst("input[name=csrf]")!!.attr("value")
    }

    override fun parseNautaUser(html: String): NautaUser {
        val htmlParsed = Jsoup.parse(html)
        val attrs = htmlParsed.selectFirst(".z-depth-1")!!.select(".m6")
        return NautaUser(
            attrs[0].selectFirst("p")!!.text().trim(),
            attrs[1].selectFirst("p")!!.text().trim(),
            attrs[2].selectFirst("p")!!.text().trim(),
            attrs[3].selectFirst("p")!!.text().trim(),
            attrs[4].selectFirst("p")!!.text().trim(),
            attrs[5].selectFirst("p")!!.text().trim(),
            attrs[6].selectFirst("p")!!.text().trim(),
            attrs[7].selectFirst("p")!!.text().trim(),
            if (attrs.size > 8) attrs[8].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[9].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[10].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[11].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[12].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[13].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[14].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[15].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[16].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[17].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[18].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[19].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[20].selectFirst("p")!!.text().trim() else null
        )
    }

    override fun parseConnectionsSummary(html: String): ConnectionsSummary {
        val htmlParsed = Jsoup.parse(html)
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

    override fun parseRechargesSummary(html: String): RechargesSummary {
        val htmlParsed = Jsoup.parse(html)
        val (recharges, totalImport) = htmlParsed.selectFirst("#content")!!.select(".card-content")
        return RechargesSummary(
            recharges.selectFirst("input[name=count]")!!.attr("value").toInt(),
            recharges.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            totalImport.selectFirst(".card-stats-number")!!.text().trim().toPriceFloat()
        )
    }

    override fun parseTransfersSummary(html: String): TransfersSummary {
        val htmlParsed = Jsoup.parse(html)
        val (transfers, totalImport) = htmlParsed.selectFirst("#content")!!.select(".card-content")
        return TransfersSummary(
            transfers.selectFirst("input[name=count]")!!.attr("value").toInt(),
            transfers.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            totalImport.selectFirst(".card-stats-number")!!.text().trim().toPriceFloat()
        )
    }

    override fun parseQuotesPaidSummary(html: String): QuotesPaidSummary {
        val htmlParsed = Jsoup.parse(html)
        val (quotesPaid, totalImport) = htmlParsed.selectFirst("#content")!!.select(".card-content")
        return QuotesPaidSummary(
            quotesPaid.selectFirst("input[name=count]")!!.attr("value").toInt(),
            quotesPaid.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            totalImport.selectFirst(".card-stats-number")!!.text().trim().toPriceFloat()
        )
    }

    override fun parseConnections(html: String): List<Connection> {
        val connections = mutableListOf<Connection>()
        val htmlParsed = Jsoup.parse(html)
        val tableBody = htmlParsed.selectFirst(".responsive-table > tbody")
        tableBody?.select("tr")?.let {
            if (it.isNotEmpty()) {
                for (row in it) {
                    val (startSessionTag, endSessionTag, durationTag, uploadedTag, downloadedTag, importTag) = row.select(
                        "td"
                    )
                    connections.add(
                        Connection(
                            startSessionTag.text().trim().toDateTime(),
                            endSessionTag.text().trim().toDateTime(),
                            durationTag.text().trim().toSeconds(),
                            uploadedTag.text().trim().toBytes(),
                            downloadedTag.text().trim().toBytes(),
                            importTag.text().trim().toPriceFloat()
                        )
                    )
                }
            }
        }
        return connections
    }

    override fun parseRecharges(html: String): List<Recharge> {
        val recharges = mutableListOf<Recharge>()
        val htmlParsed = Jsoup.parse(html)
        val tableBody = htmlParsed.selectFirst(".responsive-table > tbody")
        tableBody?.select("tr")?.let {
            if (it.isNotEmpty()) {
                for (row in it) {
                    val (dateTag, importTag, channelTag, typeTag) = row.select("td")
                    recharges.add(
                        Recharge(
                            dateTag.text().trim().toDateTime(),
                            importTag.text().trim().toPriceFloat(),
                            channelTag.text().trim(),
                            typeTag.text().trim()
                        )
                    )
                }
            }
        }
        return recharges
    }

    override fun parseTransfers(html: String): List<Transfer> {
        val transfers = mutableListOf<Transfer>()
        val htmlParsed = Jsoup.parse(html)
        val tableBody = htmlParsed.selectFirst(".responsive-table > tbody")
        tableBody?.select("tr")?.let {
            if (it.isNotEmpty()) {
                for (row in it) {
                    val (dateTag, importTag, destinyAccountTag) = row.select("td")
                    transfers.add(
                        Transfer(
                            dateTag.text().trim().toDateTime(),
                            importTag.text().trim().toPriceFloat(),
                            destinyAccountTag.text().trim()
                        )
                    )
                }
            }
        }
        return transfers
    }

    override fun parseQuotesPaid(html: String): List<QuotePaid> {
        val quotesPaid = mutableListOf<QuotePaid>()
        val htmlParsed = Jsoup.parse(html)
        val tableBody = htmlParsed.selectFirst(".responsive-table > tbody")
        tableBody?.select("tr")?.let {
            if (it.isNotEmpty()) {
                for (row in it) {
                    val (dateTag, importTag, channelTag, typeTag, officeTag) = row.select("td")
                    quotesPaid.add(
                        QuotePaid(
                            dateTag.text().trim().toDateTime(),
                            importTag.text().trim().toPriceFloat(),
                            channelTag.text().trim(),
                            typeTag.text().trim(),
                            officeTag.text().trim()
                        )
                    )
                }
            }
        }
        return quotesPaid
    }
}