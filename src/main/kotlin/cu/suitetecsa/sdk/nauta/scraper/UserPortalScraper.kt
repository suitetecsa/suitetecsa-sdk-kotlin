package cu.suitetecsa.sdk.nauta.scraper

import cu.suitetecsa.sdk.util.ExceptionHandler
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.domain.model.ResultType

/**
 * Interfaz que define un scraper para analizar información en contenido HTML relacionado con el portal de usuario.
 */
interface UserPortalScraper {
    /**
     * Analiza el HTML para extraer mensajes de error y encapsularlos en un objeto `ResultType`.
     *
     * @param html El contenido HTML a analizar.
     * @param message El mensaje de error por defecto.
     * @param exceptionHandler El gestor de excepciones a utilizar.
     * @return Un objeto `ResultType` que contiene el mensaje de error o éxito.
     */
    fun parseErrors(
        html: String,
        message: String = "nothing",
        exceptionHandler: ExceptionHandler = ExceptionHandler.builder().build()
    ): ResultType<String>

    /**
     * Analiza el HTML para extraer el token CSRF.
     *
     * @param html El contenido HTML a analizar.
     * @return El token CSRF.
     */
    fun parseCsrfToken(html: String): String

    /**
     * Analiza el HTML para extraer la información del usuario de Nauta.
     *
     * @param html El contenido HTML a analizar.
     * @param exceptionHandler El gestor de excepciones a utilizar.
     * @return El objeto `NautaUser` que contiene la información del usuario.
     */
    fun parseNautaUser(html: String, exceptionHandler: ExceptionHandler? = null): NautaUser

    /**
     * Analiza el HTML para extraer un resumen de conexiones.
     *
     * @param html El contenido HTML a analizar.
     * @return El objeto `ConnectionsSummary` que contiene el resumen de conexiones.
     */
    fun parseConnectionsSummary(html: String): ConnectionsSummary

    /**
     * Analiza el HTML para extraer un resumen de recargas.
     *
     * @param html El contenido HTML a analizar.
     * @return El objeto `RechargesSummary` que contiene el resumen de recargas.
     */
    fun parseRechargesSummary(html: String): RechargesSummary

    /**
     * Analiza el HTML para extraer un resumen de transferencias.
     *
     * @param html El contenido HTML a analizar.
     * @return El objeto `TransfersSummary` que contiene el resumen de transferencias.
     */
    fun parseTransfersSummary(html: String): TransfersSummary

    /**
     * Analiza el HTML para extraer un resumen de cotizaciones pagadas.
     *
     * @param html El contenido HTML a analizar.
     * @return El objeto `QuotesPaidSummary` que contiene el resumen de cotizaciones pagadas.
     */
    fun parseQuotesPaidSummary(html: String): QuotesPaidSummary

    /**
     * Analiza el HTML para extraer la lista de conexiones.
     *
     * @param html El contenido HTML a analizar.
     * @return La lista de objetos `Connection` que contienen la información de las conexiones.
     */
    fun parseConnections(html: String): List<Connection>

    /**
     * Analiza el HTML para extraer la lista de recargas.
     *
     * @param html El contenido HTML a analizar.
     * @return La lista de objetos `Recharge` que contienen la información de las recargas.
     */
    fun parseRecharges(html: String): List<Recharge>

    /**
     * Analiza el HTML para extraer la lista de transferencias.
     *
     * @param html El contenido HTML a analizar.
     * @return La lista de objetos `Transfer` que contienen la información de las transferencias.
     */
    fun parseTransfers(html: String): List<Transfer>

    /**
     * Analiza el HTML para extraer la lista de cotizaciones pagadas.
     *
     * @param html El contenido HTML a analizar.
     * @return La lista de objetos `QuotePaid` que contienen la información de las cotizaciones pagadas.
     */
    fun parseQuotesPaid(html: String): List<QuotePaid>
}
