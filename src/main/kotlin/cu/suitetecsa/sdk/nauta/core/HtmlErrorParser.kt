package cu.suitetecsa.sdk.nauta.core

import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlErrorParser(portalManager: PortalManager) {
    private val failReason = mapOf(
        Connect to """alert\("(?<reason>[^"]*?)"\)""",
        User to """toastr\.error\('(?<reason>.*)'\)"""
    )
    private val regex = Regex(failReason[portalManager]!!)

    fun parseError(document: Document): String? {
        val lastScript = document.select("script[type='text/javascript']").lastOrNull()
        return lastScript?.let { script ->
            val reason = regex.find(script.data().trim())?.groups?.get("reason")?.value
            reason?.let { Jsoup.parse(it).selectFirst("li[class=\"msg_error\"]")?.text() }
        }
    }
}