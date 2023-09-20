package cu.suitetecsa.sdk.util

/**
 * Implementación de la factoría de excepciones que crea instancias de excepciones utilizando una clase de excepción dada.
 *
 * @param exceptionClass La clase de excepción utilizada para crear las instancias de excepción.
 */
class ExceptionFactoryImpl(private val exceptionClass: Class<out Exception>) : ExceptionFactory {
    /**
     * Crea una instancia de excepción utilizando la clase de excepción y el mensaje dado.
     *
     * @param message El mensaje de la excepción.
     * @return Una instancia de excepción creada.
     */
    override fun createException(message: String): Exception {
        return exceptionClass.getDeclaredConstructor(String::class.java).newInstance(message)
    }
}
