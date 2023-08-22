package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.core.ExceptionHandler
import cu.suitetecsa.sdk.nauta.core.PortalManager
import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.connectDomain
import cu.suitetecsa.sdk.nauta.core.exceptions.LoadInfoException
import cu.suitetecsa.sdk.nauta.core.extensions.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.core.extensions.toSeconds
import cu.suitetecsa.sdk.nauta.framework.model.AccountInfo
import cu.suitetecsa.sdk.nauta.framework.model.LastConnection
import cu.suitetecsa.sdk.nauta.framework.model.NautaConnectInformation
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Implementación de `ConnectionInfoParser` para analizar información de conexión de Nauta.
 */
class ConnectionInfoParserImpl : ConnectionInfoParser {

    private val loadInfoExceptionHandler = ExceptionHandler.builder(LoadInfoException::class.java).build()

    /**
     * Analiza el HTML para verificar si hay conexiones disponibles.
     *
     * @param html El contenido HTML a analizar.
     * @return `true` si hay conexiones disponibles, de lo contrario, `false`.
     */
    override fun parseCheckConnections(html: String): Boolean {
        return !html.contains(connectDomain)
    }

    /**
     * Analiza el HTML para extraer información de conexión de Nauta.
     *
     * @param html El contenido HTML a analizar.
     * @return Un objeto de tipo `NautaConnectInformation` que contiene la información de conexión.
     */
    override fun parseNautaConnectInformation(html: String): NautaConnectInformation {
        val keys = listOf("account_status", "credit", "expiration_date", "access_areas", "from", "to", "time")
        val htmlParsed = Jsoup.parse(html)

        htmlParsed.throwExceptionOnFailure(
            message = "Fail parse nauta account information",
            portalManager = Connect,
            exceptionHandler = loadInfoExceptionHandler
        )

        val info = parseInfoTable(htmlParsed, keys)
        val lastConnections = parseLastConnectionsTable(htmlParsed, keys)
        return NautaConnectInformation(accountInfo = createAccountInfo(info), lastConnections = lastConnections)
    }

    /**
     * Analiza el HTML para extraer el tiempo restante de la conexión.
     *
     * @param html El contenido HTML a analizar.
     * @return El tiempo restante de la conexión en segundos.
     */
    override fun parseRemainingTime(html: String): Long {
        try {
            return html.toSeconds()
        } catch (exc: Exception) {
            exc.printStackTrace()
            throw loadInfoExceptionHandler.handleException("Fail to parse remaining time", listOf(exc.message ?: ""))
        }
    }

    /**
     * Analiza el HTML para extraer el atributo UUID.
     *
     * @param html El contenido HTML a analizar.
     * @return El valor del atributo UUID.
     */
    override fun parseAttributeUUID(html: String): String {
        Regex("ATTRIBUTE_UUID=(\\w+)&").find(html)?.groupValues?.get(1)?.let {
            return it
        } ?: run {
            throw loadInfoExceptionHandler.handleException("Fail to parse attribute uuid", listOf(html.take(100)))
        }
    }

    /**
     * Analiza el HTML para verificar si el cierre de sesión fue exitoso.
     *
     * @param html El contenido HTML a analizar.
     * @return `true` si el cierre de sesión fue exitoso, de lo contrario, `false`.
     */
    override fun isSuccessLogout(html: String) = html.contains("SUCCESS")

    /**
     * Analiza la tabla de información en el HTML y extrae los valores asociados a las claves dadas.
     *
     * @param htmlParsed El objeto `Document` parseado del contenido HTML.
     * @param keys Las claves que se utilizan para asociar los valores en la tabla.
     * @return Un mapa de pares clave-valor con la información extraída de la tabla.
     */
    private fun parseInfoTable(htmlParsed: Document, keys: List<String>): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val valueElements = htmlParsed.select("#sessioninfo > tbody > tr > :not(td.key)")
        valueElements.take(keys.size).forEachIndexed { index, element ->
            info[keys[index]] = element.text().trim()
        }
        return info
    }

    /**
     * Analiza la tabla de últimas conexiones en el HTML y extrae los valores de las columnas especificadas.
     *
     * @param htmlParsed El objeto `Document` parseado del contenido HTML.
     * @param keys Las claves que se utilizan para asociar los valores en la tabla.
     * @return Una lista de objetos `LastConnection` con la información de las últimas conexiones.
     */
    private fun parseLastConnectionsTable(htmlParsed: Document, keys: List<String>): List<LastConnection> {
        val lastConnections = mutableListOf<LastConnection>()
        val connectionRows = htmlParsed.select("#sesiontraza > tbody > tr")
        for (row in connectionRows) {
            val connectionValues = row.select("td")
            val connectionMap = connectionValues.take(keys.size - 4).mapIndexed { index, element ->
                keys[index + 4] to element.text().trim()
            }.toMap()
            lastConnections.add(
                LastConnection(
                    from = connectionMap["from"] ?: "",
                    time = connectionMap["time"] ?: "",
                    to = connectionMap["to"] ?: ""
                )
            )
        }
        return lastConnections
    }

    /**
     * Crea un objeto `AccountInfo` a partir del mapa de información proporcionado.
     *
     * @param info El mapa de información con claves y valores asociados.
     * @return Un objeto `AccountInfo` con la información de la cuenta.
     */
    private fun createAccountInfo(info: Map<String, String>): AccountInfo {
        return AccountInfo(
            accessAreas = info["access_areas"] ?: "",
            accountStatus = info["account_status"] ?: "",
            credit = info["credit"] ?: "",
            expirationDate = info["expiration_date"] ?: ""
        )
    }
}
