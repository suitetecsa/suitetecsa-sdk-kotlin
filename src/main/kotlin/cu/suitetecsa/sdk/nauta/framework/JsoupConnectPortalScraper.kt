package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.nauta.core.connectDomain
import cu.suitetecsa.sdk.nauta.core.connectPortal
import cu.suitetecsa.sdk.nauta.core.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.framework.model.AccountInfo
import cu.suitetecsa.sdk.nauta.framework.model.LastConnection
import cu.suitetecsa.sdk.nauta.framework.model.NautaConnectInformation
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

internal class JsoupConnectPortalScraper : ConnectPortalScraper {
    private fun getInputs(formSoup: Element): Map<String, String> {
        val inputs = mutableMapOf<String, String>()
        for (input in formSoup.select("input[name]")) {
            inputs[input.attr("name")] = input.attr("value")
        }
        return inputs
    }

    override fun parseErrors(html: String): ResultType<String> {
        val htmlParsed = Jsoup.parse(html)
        return try {
            htmlParsed.throwExceptionOnFailure(Exception::class.java, "nothing", connectPortal)
            ResultType.Success(html)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }

    override fun parseCheckConnections(html: String): Boolean {
        return !html.contains(connectDomain)
    }

    override fun parseActionForm(html: String): Pair<String, Map<String, String>> {
        val htmlForm = Jsoup.parse(html).selectFirst("form[action]")
        htmlForm?.let {
            val data = getInputs(it)
            val url = it.attr("action")
            return Pair(url, data)
        }
        return Pair("", mapOf("" to ""))
    }

    override fun parseLoginForm(html: String): Pair<String, Map<String, String>> {
        val htmlForm = Jsoup.parse(html).selectFirst("form.form")
        htmlForm?.let {
            val data = getInputs(it)
            val url = it.attr("action")
            return Pair(url, data)
        }
        return Pair("", mapOf("" to ""))
    }

    override fun parseNautaConnectInformation(html: String): NautaConnectInformation {
        val keys = listOf("account_status", "credit", "expiration_date", "access_areas", "from", "to", "time")
        val htmlParsed = Jsoup.parse(html)
        val info = mutableMapOf<String, String>()
        val lastConnections = mutableListOf<LastConnection>()
        for ((index, value) in htmlParsed.select("#sessioninfo > tbody > tr > :not(td.key)").withIndex()) {
            info[keys[index]] = value.text().trim()
        }
        val accountInfo = AccountInfo(
            accessAreas = info["access_areas"]!!,
            accountStatus = info["account_status"]!!,
            credit = info["credit"]!!,
            expirationDate = info["expiration_date"]!!
        )
        for (tr in htmlParsed.select("#sesiontraza > tbody > tr")) {
            val connection = mutableMapOf<String, String>()
            for ((index, value) in tr.select("td").withIndex()) {
                connection[keys[index + 4]] = value.text().trim()
            }
            lastConnections.add(
                LastConnection(
                    from = connection["from"]!!,
                    time = connection["time"]!!,
                    to = connection["to"]!!
                )
            )
        }
        return NautaConnectInformation(accountInfo = accountInfo, lastConnections = lastConnections)
    }

    override fun parseRemainingTime(html: String): Long {
        return html.toSeconds()
    }

    override fun parseAttributeUUID(html: String): String {
        return Regex("ATTRIBUTE_UUID=(\\w+)&").find(html)?.groupValues?.get(1) ?: ""
    }

    override fun isSuccessLogout(html: String): Boolean {
        return html.contains("SUCCESS")
    }
}