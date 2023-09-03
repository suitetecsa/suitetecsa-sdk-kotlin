package cu.suitetecsa.sdk.nauta.scraper

/**
 * Implementación de `ConnectPortalScraper` que utiliza parsers específicos para extraer información del HTML.
 *
 * @param errorParser El parser de errores a utilizar.
 * @param formParser El parser de formularios a utilizar.
 * @param connectionInfoParser El parser de información de conexión a utilizar.
 */
class ConnectPortalScraperImpl(
    private val errorParser: ErrorParser,
    private val formParser: FormParser,
    private val connectionInfoParser: ConnectionInfoParser
) : ConnectPortalScraper {
    /**
     * Analiza el HTML para extraer mensajes de error y encapsularlos en un objeto `ResultType`.
     */
    override fun parseErrors(html: String) = errorParser.parseErrors(html)

    /**
     * Analiza el HTML para verificar si hay conexiones disponibles.
     */
    override fun parseCheckConnections(html: String) = connectionInfoParser.parseCheckConnections(html)

    /**
     * Analiza el HTML para extraer información y datos del formulario de acción.
     */
    override fun parseActionForm(html: String) = formParser.parseActionForm(html)

    /**
     * Analiza el HTML para extraer información y datos del formulario de inicio de sesión.
     */
    override fun parseLoginForm(html: String) = formParser.parseLoginForm(html)

    /**
     * Analiza el HTML para extraer información de conexión de Nauta.
     */
    override fun parseNautaConnectInformation(html: String) = connectionInfoParser.parseNautaConnectInformation(html)

    /**
     * Analiza el HTML para extraer el tiempo restante de la conexión.
     */
    override fun parseRemainingTime(html: String) = connectionInfoParser.parseRemainingTime(html)

    /**
     * Analiza el HTML para extraer el atributo UUID.
     */
    override fun parseAttributeUUID(html: String) = connectionInfoParser.parseAttributeUUID(html)

    /**
     * Analiza el HTML para verificar si el cierre de sesión fue exitoso.
     */
    override fun isSuccessLogout(html: String) = connectionInfoParser.isSuccessLogout(html)

    class Builder {
        private var errorParser: ErrorParser? = null
        private var formParser: FormParser? = null
        private var connectionInfoParser: ConnectionInfoParser? = null

        fun errorParser(parser: ErrorParser): Builder {
            errorParser = parser
            return this
        }
        fun formParser(parser: FormParser): Builder {
            formParser = parser
            return this
        }
        fun connectionInfoParser(parser: ConnectionInfoParser): Builder {
            connectionInfoParser = parser
            return this
        }

        private fun createErrorParser(): ErrorParser {
            return ErrorParserImpl()
        }
        private fun createFormParser(): FormParser {
            return FormParserImpl()
        }
        private fun createConnectionInfoParser(): ConnectionInfoParser {
            return ConnectionInfoParserImpl()
        }

        fun build(): ConnectPortalScraper {
            return ConnectPortalScraperImpl(
                errorParser = errorParser ?: createErrorParser(),
                formParser = formParser ?: createFormParser(),
                connectionInfoParser = connectionInfoParser ?: createConnectionInfoParser()
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
