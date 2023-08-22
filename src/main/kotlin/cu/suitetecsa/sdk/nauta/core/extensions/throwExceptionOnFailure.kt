package cu.suitetecsa.sdk.nauta.core.extensions

import cu.suitetecsa.sdk.nauta.core.*
import org.jsoup.Connection
import org.jsoup.nodes.Document

internal fun Document.throwExceptionOnFailure(
    message: String,
    portalManager: PortalManager,
    exceptionHandler: ExceptionHandler? = null
) {
    val errorParser = HtmlErrorParser(portalManager)
    val error = errorParser.parseError(this)

    error?.let {
        val errors = if (it.startsWith("Se han detectado algunos errores.")) {
            val subMessages = select("li[class='sub-message']").map { subMessage -> subMessage.text() }
            subMessages
        } else {
            listOf(it)
        }
        exceptionHandler?.let { handler ->
            throw handler.handleException(message = message, errors = errors)
        } ?: run {
            throw ExceptionHandler.builder().build().handleException(message, errors)
        }
    }
}

internal fun Connection.Response.throwExceptionOnFailure(
    message: String,
    exceptionFactory: ExceptionFactory? = null
) {
    val statusCode = this.statusCode()
    if (statusCode !in 200..299 && !(statusCode in 300..399 && this.hasHeader("Location"))) {
        exceptionFactory?.let {
            throw ExceptionHandler(it).handleException(message, listOf(this.statusMessage()))
        } ?: run {
            throw ExceptionHandler.builder().build().handleException(message, listOf(this.statusMessage()))
        }
    }
}