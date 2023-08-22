package cu.suitetecsa.sdk.nauta.core

class ExceptionFactoryImpl(private val exceptionClass: Class<out Exception>) : ExceptionFactory {
    override fun createException(message: String): Exception {
        return exceptionClass.getDeclaredConstructor(String::class.java).newInstance(message)
    }
}