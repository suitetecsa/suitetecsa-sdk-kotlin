package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.framework.model.ResultType

/**
 * Interfaz que define un parser para analizar mensajes de error en contenido HTML.
 */
interface ErrorParser {
    /**
     * Analiza el HTML para extraer mensajes de error y encapsularlos en un objeto `ResultType`.
     *
     * @param html El contenido HTML a analizar.
     * @return Un objeto `ResultType` que contiene el mensaje de error o éxito.
     */
    fun parseErrors(html: String): ResultType<String>
}
