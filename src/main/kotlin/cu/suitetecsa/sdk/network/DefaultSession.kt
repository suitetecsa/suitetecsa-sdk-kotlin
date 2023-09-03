package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.nauta.domain.model.ResultType
import org.jsoup.Connection

/**
 * Esta clase representa una sesión de comunicación con el portal Nauta (Portal Cautivo, Portal de Usuario).
 * Mantiene las cookies para permitir una comunicación continua. Utiliza la biblioteca `Jsoup`.
 *
 * @param connectionFactory Constructor de conexiones (`org.jsoup.Connection`) utilizado para establecer la comunicación.
 */
internal class DefaultSession(
    private val connectionFactory: ConnectionFactory
) : Session {
    private val cookies: MutableMap<String, String> = mutableMapOf()

    /**
     * Realiza una solicitud GET al portal Nauta.
     *
     * @param url URL a la que se realiza la solicitud.
     * @param params Parámetros de la solicitud (opcional).
     * @param ignoreContentType Ignorar el tipo de contenido devuelto en la respuesta (por defecto: `false`).
     * @param timeout Tiempo límite para la solicitud (por defecto: `30000` milisegundos).
     * @return Objeto `ResultType<HttpResponse>` con los datos de la respuesta o información sobre el error, según corresponda.
     */
    override fun get(
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int
    ): ResultType<HttpResponse> {
        return executeRequest(url, params) { connection ->
            connection
                .ignoreContentType(ignoreContentType)
                .timeout(timeout)
                .method(Connection.Method.GET)
                .execute()
        }
    }

    /**
     * Realiza una solicitud POST al portal Nauta.
     *
     * @param url URL a la que se realiza la solicitud.
     * @param data Datos de la solicitud (opcional).
     * @return Objeto `ResultType<HttpResponse>` con los datos de la respuesta o información sobre el error, según corresponda.
     */
    override fun post(url: String, data: Map<String, String>?): ResultType<HttpResponse> {
        return executeRequest(url, data) { connection ->
            connection.method(Connection.Method.POST).execute()
        }
    }

    /**
    * Crea y ejecuta una conexión con los parámetros proporcionados.
    *
    * @param url URL de la solicitud.
    * @param requestData Datos para la solicitud.
    * @param requestAction Función lambda que ejecuta la solicitud y devuelve la respuesta.
    * @return Objeto `ResultType<HttpResponse>` con los datos de la respuesta o información sobre el error, según corresponda.
    */
    private fun executeRequest(
        url: String,
        requestData: Map<String, String>? = null,
        requestAction: (Connection) -> Connection.Response
    ): ResultType<HttpResponse> {
        return try {
            val response = connectionFactory.createConnection(url, requestData, cookies).let(requestAction)
            response.throwExceptionOnFailure("There was a failure to communicate with the portal")
            response.cookies().forEach { (name, value) -> cookies[name] = value }
            ResultType.Success(
                HttpResponse(
                    statusCode = response.statusCode(),
                    statusMessage = response.statusMessage(),
                    content = response.bodyAsBytes(),
                    cookies = response.cookies()
                )
            )
        } catch (e: Exception) {
            ResultType.Failure(e)
        }
    }

    /**
     * Builder para construir una instancia de `DefaultSession`.
     */
    class Builder {
        private var connectionFactory: ConnectionFactory? = null

        /**
         * Establece la factoría de conexiones para la sesión.
         *
         * @param factory La factoría de conexiones a utilizar.
         * @return El builder actualizado.
         */
        fun connectionFactory(factory: ConnectionFactory): Builder {
            connectionFactory = factory
            return this
        }

        /**
         * Crea una instancia de `DefaultSession` utilizando la factoría de conexiones especificada.
         *
         * @return La instancia de `DefaultSession` creada.
         */
        fun build(): Session {
            val factory = connectionFactory ?: JsoupConnectionFactory()
            return DefaultSession(factory)
        }
    }
}