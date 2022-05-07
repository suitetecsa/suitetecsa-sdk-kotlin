package cu.suitetecsa.kotlibsuitetecsa.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

enum class Portal {
    NAUTA,
    USER_PORTAL
}

private val fail_reason = mapOf(
    Portal.NAUTA to """alert\("(?<reason>[^"]*?)"\)""",
    Portal.USER_PORTAL to """toastr\.error\('(?<reason>[^']*?)'\)"""
)

fun findError(soup: Document, portal: Portal): List<String> {
    val lastScript = soup.select("script[type='text/javascript']").last()?.data()
    val reason = fail_reason[portal]?.let {
        lastScript?.let { it1 ->
            Regex(
                it
            ).matchEntire(it1.trim())?.groups?.get("reason")?.value
        }
    }
    if (portal == Portal.NAUTA) {
        if (reason != null) {
            return listOf(reason)

        }
    } else {
        val error = reason?.let {
            Jsoup.parse(it).select("li[class='msg_error']").first()
        }
        val errors = mutableListOf<String>()
        if (error != null) {
            if (error.text().startsWith("Se han detectado algunos errores.")) {
                val subMessages = error.select("li[class='sub-message']")
                for (subMessage in subMessages) {
                    errors.add(subMessage.text())
                }
            } else {
                errors.add(error.text())
            }
        }
        return errors
    }
    return emptyList()
}