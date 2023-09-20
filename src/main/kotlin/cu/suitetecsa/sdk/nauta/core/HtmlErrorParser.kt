package cu.suitetecsa.sdk.nauta.core

import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Utility class for parsing error messages from an HTML document.
 *
 * @param portalManager The portal manager associated with the error messages.
 */
class HtmlErrorParser(private val portalManager: PortalManager) {
    private val failReason = mapOf(
        Connect to """alert\("(?<reason>[^"]*?)"\)""",
        User to """toastr\.error\('(?<reason>.*)'\)"""
    )
    private val regex = Regex(failReason[portalManager]!!)

    /**
     * Parses the error message from the given HTML document.
     *
     * @param document The HTML document to parse.
     * @return The parsed error message, or null if no error message is found.
     */
    fun parseError(document: Document): String? {
        val lastScript = document.select("script[type='text/javascript']").lastOrNull()
        return lastScript?.let { script ->
            val reason = regex.find(script.data().trim())?.groups?.get("reason")?.value
            reason?.let { Jsoup.parse(it).selectFirst("li[class=\"msg_error\"]")?.text() }
        }
    }
}
