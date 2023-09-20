package cu.suitetecsa.sdk.nauta.scraper

import cu.suitetecsa.sdk.nauta.domain.model.NautaConnectInformation
import cu.suitetecsa.sdk.nauta.domain.model.ResultType

/**
 * Interfaz que define un scraper para analizar contenido HTML en el portal de conexión.
 * Proporciona métodos para extraer información específica y transformarla en objetos y resultados.
 */
interface ConnectPortalScraper {
    /**
     * Analiza el HTML para extraer mensajes de error y encapsularlos en un objeto `ResultType`.
     *
     * @param html El contenido HTML a analizar.
     * @return Un objeto `ResultType` que contiene el mensaje de error o éxito.
     */
    fun parseErrors(html: String): ResultType<String>

    /**
     * Analiza el HTML para verificar si hay conexiones disponibles.
     *
     * @param html El contenido HTML a analizar.
     * @return `true` si hay conexiones disponibles, de lo contrario, `false`.
     */
    fun parseCheckConnections(html: String): Boolean

    /**
     * Analiza el HTML para extraer información y datos del formulario de acción.
     *
     * @param html El contenido HTML a analizar.
     * @return Un par que contiene la URL del formulario y un mapa de datos.
     */
    fun parseActionForm(html: String): Pair<String, Map<String, String>>

    /**
     * Analiza el HTML para extraer información y datos del formulario de inicio de sesión.
     *
     * @param html El contenido HTML a analizar.
     * @return Un par que contiene la URL del formulario y un mapa de datos.
     */
    fun parseLoginForm(html: String): Pair<String, Map<String, String>>

    /**
     * Analiza el HTML para extraer información de conexión de Nauta.
     *
     * @param html El contenido HTML a analizar.
     * @return Un objeto de tipo `NautaConnectInformation` que contiene la información de conexión.
     */
    fun parseNautaConnectInformation(html: String): NautaConnectInformation

    /**
     * Analiza el HTML para extraer el tiempo restante de la conexión.
     *
     * @param html El contenido HTML a analizar.
     * @return El tiempo restante de la conexión en segundos.
     */
    fun parseRemainingTime(html: String): Long

    /**
     * Analiza el HTML para extraer el atributo UUID.
     *
     * @param html El contenido HTML a analizar.
     * @return El valor del atributo UUID.
     */
    fun parseAttributeUUID(html: String): String

    /**
     * Analiza el HTML para verificar si el cierre de sesión fue exitoso.
     *
     * @param html El contenido HTML a analizar.
     * @return `true` si el cierre de sesión fue exitoso, de lo contrario, `false`.
     */
    fun isSuccessLogout(html: String): Boolean
}
