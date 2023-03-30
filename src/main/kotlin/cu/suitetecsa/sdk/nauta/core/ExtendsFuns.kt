package cu.suitetecsa.sdk.nauta.core

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

fun Connection.Response.throwExceptionOnFailure(exc: Class<out Exception>, msg: String) {
    val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
    if (this.statusCode() != 200) {
        throw exceptionConstructor.newInstance(
            "$msg :: ${this.statusMessage()}"
        )
    }
}

fun Document.throwExceptionOnFailure(exc: Class<out Exception>, msg: String, portalManager: Portal) {
    val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
    val errors = mutableListOf<String>()
        val lastScript = this.select("script[type='text/javascript']").last()
        if (lastScript != null) {
            val failReason = mapOf(
                Portal.CONNECT to """alert\("(?<reason>[^"]*?)"\)""",
                Portal.USER to """toastr\.error\('(?<reason>.*)'\)"""
            )
            val regex = Regex(failReason[portalManager]!!)
            val reason = regex.find(lastScript.data().trim())?.groups?.get("reason")?.value
            if (portalManager == Portal.CONNECT && reason != null) {
                throw exceptionConstructor.newInstance(
                    "$msg :: $reason"
                )
            } else {
                val error = reason?.let { Jsoup.parse(it).selectFirst("li[class=\"msg_error\"]") }
                if (error != null) {
                    if (error.text().startsWith("Se han detectado algunos errores.")) {
                        val subMessages = error.select("li[class='sub-message']")
                        for (subMessage in subMessages) {
                            errors.add(subMessage.text())
                        }
                        throw exceptionConstructor.newInstance(
                            "$msg :: $errors"
                        )
                    } else {
                        throw exceptionConstructor.newInstance(
                            "$msg :: ${error.text()}"
                        )
                    }
                }
            }
        }
}

operator fun Elements.component6(): Element {
    return this[5]
}