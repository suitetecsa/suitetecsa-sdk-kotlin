package cu.suitetecsa.sdk.nauta.core.extensions

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.util.ExceptionHandler
import org.jsoup.nodes.Document

/**
 * Throws an exception if the document indicates a failure based on the error message.
 *
 * @param message The error message to include in the exception.
 * @param portalManager The portal manager used for error parsing.
 * @param exceptionHandler The exception handler to handle the exception (optional).
 */
internal fun Document.throwExceptionOnFailure(
    message: String,
    portalManager: PortalManager,
    exceptionHandler: ExceptionHandler? = null
) {
    val errorParser = HtmlErrorParser(portalManager)
    val error = errorParser.parseError(this)

    error?.let {
        val errors = if (it.startsWith("Se han detectado algunos errores.")) {
            select("li[class='sub-message']").map { subMessage -> subMessage.text() }
        } else {
            listOf(it)
        }
        exceptionHandler?.let { handler ->
            throw handler.handleException(message = message, errors = errors)
        } ?: throw ExceptionHandler.builder().build().handleException(message, errors)
    }
}
