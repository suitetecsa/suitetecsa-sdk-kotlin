package cu.suitetecsa.sdk.nauta.core

interface ExceptionFactory {
    fun createException(message: String): Exception
}