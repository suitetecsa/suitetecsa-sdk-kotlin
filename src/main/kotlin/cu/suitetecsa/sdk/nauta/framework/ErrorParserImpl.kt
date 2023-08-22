package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.core.PortalManager
import cu.suitetecsa.sdk.nauta.core.extensions.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import org.jsoup.Jsoup

/**
 * Implementación de `ErrorParser` que analiza mensajes de error en contenido HTML.
 */
internal class ErrorParserImpl : ErrorParser {
    /**
     * Analiza el HTML para extraer mensajes de error y encapsularlos en un objeto `ResultType`.
     *
     * @param html El contenido HTML a analizar.
     * @return Un objeto `ResultType` que contiene el mensaje de error o éxito.
     */
    override fun parseErrors(html: String): ResultType<String> {
        val htmlParsed = Jsoup.parse(html)
        return try {
            htmlParsed.throwExceptionOnFailure("nothing", PortalManager.Connect)
            ResultType.Success(html)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }
}
