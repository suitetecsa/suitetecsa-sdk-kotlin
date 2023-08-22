package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

/**
 * Esta clase representa una sesión de comunicación con el portal Nauta (Portal Cautivo, Portal de Usuario).
 * Mantiene las cookies para permitir una comunicación continua.
 */
interface NautaSession {
    /**
     * Realiza una solicitud GET al portal Nauta.
     *
     * @param url URL a la que se realiza la solicitud.
     * @param params Parámetros de la solicitud (opcional).
     * @param ignoreContentType Ignorar el tipo de contenido devuelto en la respuesta (por defecto: `false`).
     * @param timeout Tiempo límite para la solicitud (por defecto: `30000` milisegundos).
     * @return Objeto `ResultType<HttpResponse>` con los datos de la respuesta o información sobre el error, según corresponda.
     */
    fun get(
        url: String,
        params: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int = 30000
    ): ResultType<HttpResponse>

    /**
     * Realiza una solicitud POST al portal Nauta.
     *
     * @param url URL a la que se realiza la solicitud.
     * @param data Datos de la solicitud (opcional).
     * @return Objeto `ResultType<HttpResponse>` con los datos de la respuesta o información sobre el error, según corresponda.
     */
    fun post(url: String, data: Map<String, String>? = null): ResultType<HttpResponse>
}