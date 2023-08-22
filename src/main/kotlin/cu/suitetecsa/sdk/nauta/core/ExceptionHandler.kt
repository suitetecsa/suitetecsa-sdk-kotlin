package cu.suitetecsa.sdk.nauta.core

class ExceptionHandler(private val exceptionFactory: ExceptionFactory) {
    fun handleException(message: String, errors: List<String>): Exception {
        val errorMessage = if (errors.isNotEmpty()) {
            errors.joinToString("; ")
        } else {
            "No specific error message"
        }
        return exceptionFactory.createException("$message :: $errorMessage")
    }

    class Builder(private val exceptionClass: Class<out Exception>) {
        private var excFactory: ExceptionFactory? = null

        fun exceptionFactory(exceptionFactory: ExceptionFactory): Builder {
            excFactory = exceptionFactory
            return this
        }

        fun build(): ExceptionHandler {
            val exceptionFactory = excFactory ?: ExceptionFactoryImpl(exceptionClass)
            return ExceptionHandler(exceptionFactory)
        }
    }

    companion object {
        fun builder(exceptionClass: Class<out Exception> = Exception::class.java): Builder {
            return Builder(exceptionClass = exceptionClass)
        }
    }
}
