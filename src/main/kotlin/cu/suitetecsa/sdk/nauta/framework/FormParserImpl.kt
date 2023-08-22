package cu.suitetecsa.sdk.nauta.framework

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Implementación de `FormParser` que analiza formularios en contenido HTML.
 */
internal class FormParserImpl : FormParser {
    /**
     * Analiza el HTML para extraer información y datos del formulario de acción.
     *
     * @param html El contenido HTML a analizar.
     * @return Un par que contiene la URL del formulario y un mapa de datos.
     */
    override fun parseActionForm(html: String): Pair<String, Map<String, String>> {
        val htmlForm = Jsoup.parse(html).selectFirst("form[action]")
        htmlForm?.let {
            val data = getInputs(it)
            val url = it.attr("action")
            return Pair(url, data)
        }
        return Pair("", mapOf("" to ""))
    }

    /**
     * Analiza el HTML para extraer información y datos del formulario de inicio de sesión.
     *
     * @param html El contenido HTML a analizar.
     * @return Un par que contiene la URL del formulario y un mapa de datos.
     */
    override fun parseLoginForm(html: String): Pair<String, Map<String, String>> {
        val htmlForm = Jsoup.parse(html).selectFirst("form.form")
        htmlForm?.let {
            val data = getInputs(it)
            val url = it.attr("action")
            return Pair(url, data)
        }
        return Pair("", mapOf("" to ""))
    }

    /**
     * Obtiene los campos de entrada y sus valores del formulario.
     *
     * @param formSoup El objeto `Element` que representa el formulario HTML.
     * @return Un mapa que contiene los nombres de los campos y sus valores asociados.
     */
    private fun getInputs(formSoup: Element): Map<String, String> {
        val inputs = mutableMapOf<String, String>()
        for (input in formSoup.select("input[name]")) {
            inputs[input.attr("name")] = input.attr("value")
        }
        return inputs
    }
}
