package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.util.ExceptionFactory
import cu.suitetecsa.sdk.util.ExceptionHandler
import org.jsoup.Connection

/**
 * Throws an exception if the response indicates a failure based on the status code.
 *
 * @param message The error message to include in the exception.
 * @param exceptionFactory The factory to create the exception (optional).
 */
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