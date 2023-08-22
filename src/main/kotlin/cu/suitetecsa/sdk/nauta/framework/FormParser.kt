package cu.suitetecsa.sdk.nauta.framework

/**
 * Interfaz que define un parser para analizar formularios en contenido HTML.
 */
interface FormParser {
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
}
