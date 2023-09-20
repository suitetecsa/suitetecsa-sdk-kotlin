package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.nauta.domain.model.ResultType

/**
 * Clase que implementa el comunicador del portal utilizando Jsoup.
 *
 * @param session La sesión utilizada para la comunicación con el portal.
 */
class JsoupPortalCommunicator(
    private val session: Session
) : PortalCommunicator {
    /**
     * Maneja la respuesta de una solicitud al portal y la transforma según una función dada.
     *
     * @param url La URL a la que se realiza la solicitud.
     * @param data Datos para la solicitud (opcional).
     * @param method El método HTTP utilizado para la solicitud (por defecto es GET).
     * @param ignoreContentType Indica si se debe ignorar el tipo de contenido en la respuesta (por defecto es `false`).
     * @param timeout El tiempo límite para la solicitud en milisegundos (por defecto es 30000).
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Resultado encapsulado en un objeto `ResultType` que contiene el resultado transformado o un error.
     */
    private fun <T> handleResponse(
        url: String,
        data: Map<String, String>? = null,
        method: HttpMethod = HttpMethod.GET,
        ignoreContentType: Boolean = false,
        timeout: Int = 30000,
        transform: (HttpResponse) -> T
    ): ResultType<T> {
        val response = when (method) {
            HttpMethod.POST -> session.post(url, data)
            HttpMethod.GET -> session.get(url, data, ignoreContentType, timeout)
        }

        return when (response) {
            is ResultType.Failure -> ResultType.Failure(response.throwable)
            is ResultType.Success -> ResultType.Success(transform(response.result))
        }
    }

    /**
     * Realiza una acción en el portal de conexión y transforma la respuesta utilizando la sesión dada.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    override fun <T> performAction(action: Action, transform: (HttpResponse) -> T): ResultType<T> {
        return handleResponse(
            url = action.url(),
            data = action.data(),
            method = action.method(),
            ignoreContentType = action.ignoreContentType(),
            timeout = action.timeout(),
            transform = transform
        )
    }

    /**
     * Realiza una acción en el portal de conexión y transforma la respuesta utilizando la URL dada.
     *
     * @param url La URL a la que se realiza la solicitud.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    override fun <T> performAction(url: String, transform: (HttpResponse) -> T): ResultType<T> {
        return handleResponse(
            url = url,
            transform = transform
        )
    }

    /**
     * Builder para construir una instancia de `JsoupPortalCommunicator`.
     */
    class Builder {
        private var session: Session? = null

        /**
         * Establece la sesión utilizada para la comunicación con el portal.
         *
         * @param session La sesión a utilizar.
         * @return El builder actualizado.
         */
        fun session(session: Session): Builder {
            this.session = session
            return this
        }

        /**
         * Crea una instancia de `JsoupPortalCommunicator` utilizando la sesión especificada.
         *
         * @return La instancia de `JsoupPortalCommunicator` creada.
         */
        fun build(): JsoupPortalCommunicator {
            return JsoupPortalCommunicator(session ?: DefaultSession.Builder().build())
        }
    }
}
