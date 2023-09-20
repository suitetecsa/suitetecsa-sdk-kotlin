package cu.suitetecsa.sdk.nauta.scraper

import cu.suitetecsa.sdk.nauta.domain.model.NautaConnectInformation

/**
 * Interfaz que define un parser para analizar información relacionada con la conexión.
 * Proporciona métodos para analizar HTML y extraer información específica.
 */
interface ConnectionInfoParser {
    /**
     * Analiza el HTML para verificar si hay conexiones disponibles.
     *
     * @param html El contenido HTML a analizar.
     * @return `true` si hay conexiones disponibles, de lo contrario, `false`.
     */
    fun parseCheckConnections(html: String): Boolean

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
     * @return El tiempo restante de la conexión en milisegundos.
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
