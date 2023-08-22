package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.HttpMethod
import cu.suitetecsa.sdk.nauta.core.HttpMethod.GET
import cu.suitetecsa.sdk.nauta.core.HttpMethod.POST
import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import cu.suitetecsa.sdk.nauta.core.exceptions.NautaAttributeException
import cu.suitetecsa.sdk.nauta.core.exceptions.NautaGetInfoException
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.framework.model.ResultType.Error
import cu.suitetecsa.sdk.nauta.framework.model.ResultType.Success
import kotlin.math.ceil

/**
 * Implementación de un comunicador para interactuar con el portal de usuario.
 * Permite cargar el token CSRF, realizar acciones y transformar las respuestas.
 *
 * @param nautaSession Sesión de conexión con el portal Nauta.
 */
class UserPortalCommunicatorImpl(
    private val nautaSession: NautaSession
) : UserPortalCommunicator {

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
        method: HttpMethod = GET,
        ignoreContentType: Boolean = false,
        timeout: Int = 30000,
        transform: (HttpResponse) -> T
    ): ResultType<T> {
        val response = when (method) {
            POST -> nautaSession.post(url, data)
            GET -> nautaSession.get(url, data, ignoreContentType, timeout)
        }

        return when (response) {
            is Error -> Error(response.throwable)
            is Success -> Success(transform(response.result))
        }
    }


    /**
     * Realiza una acción en el portal de usuario y transforma la respuesta.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Resultado encapsulado en un objeto `ResultType`.
     * @throws NautaAttributeException sí se intenta realizar una acción no permitida en el portal de conexión.
     * @throws NautaGetInfoException si la acción de obtener acciones no está permitida.
     */
    override fun <T> performAction(action: Action, transform: (HttpResponse) -> T): ResultType<T> {
        val url = when (action.portalManager) {
            Connect -> throw NautaAttributeException("Acción no permitida en el portal de conexión.")
            User -> "${action.portalManager.baseUrl}${action.route}"
        }

        return when (action) {
            is Action.GetActions -> throw NautaGetInfoException("Acción no permitida para obtener acciones.")
            else -> handleResponse(
                url = url,
                data = action.data,
                method = action.method,
                ignoreContentType = action.ignoreContentType,
                timeout = action.timeout,
                transform = transform
            )
        }
    }

    /**
     * Realiza una acción en el portal de usuario que devuelve una lista y transforma las respuestas.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a cada respuesta del portal.
     * @return Resultado encapsulado en un objeto `ResultType`.
     * @throws NautaAttributeException sí se intenta realizar una acción no permitida en el portal de conexión.
     * @throws NautaGetInfoException si la acción de obtener acciones no está permitida.
     */
    override fun <T> performListAction(action: Action, transform: (HttpResponse) -> List<T>): ResultType<List<T>> {
        val url = when (action.portalManager) {
            Connect -> throw NautaAttributeException("Acción no permitida en el portal de conexión.")
            User -> "${action.portalManager.baseUrl}${action.route}"
        }

        return when (action) {
            is Action.GetActions -> getActions(
                url = url,
                count = action.count,
                yearMonthSelected = action.yearMonthSelected,
                large = action.large,
                reversed = action.reversed,
                transform = transform
            )
            else -> throw NautaGetInfoException("Acción no permitida para obtener información.")
        }
    }

    /**
     * Carga el token CSRF desde la URL proporcionada y transforma la respuesta.
     *
     * @param url La URL desde la cual se cargará el token CSRF.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Resultado encapsulado en un objeto `ResultType`.
     */
    override fun <T> loadCsrf(url: String, transform: (HttpResponse) -> T): ResultType<T> {
        return handleResponse(url, transform = transform)
    }

    /**
     * Realiza una acción en el portal de usuario que devuelve una lista y transforma las respuestas.
     *
     * @param url La URL base para las acciones en el portal.
     * @param count El número total de acciones disponibles.
     * @param yearMonthSelected El año y mes seleccionado para filtrar las acciones.
     * @param large La cantidad máxima de elementos a obtener.
     * @param reversed Indica si las páginas deben ser recorridas en orden inverso.
     * @param transform La función de transformación que se aplicará a cada respuesta del portal.
     * @return Resultado encapsulado en un objeto `ResultType` que contiene una lista de elementos transformados.
     */
    private fun <T> getActions(
        url: String,
        count: Int,
        yearMonthSelected: String,
        large: Int,
        reversed: Boolean,
        transform: (HttpResponse) -> List<T>
    ): ResultType<List<T>> {
        val actionList = mutableListOf<T>()
        val internalLarge = if (large == 0 || large > count) count else large
        if (count != 0) {
            val totalPages = ceil(count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while ((actionList.size - rest) < internalLarge && (currentPage in 1..totalPages)) {
                val page = if (currentPage != 1) currentPage else null
                val currentUrl = "$url$yearMonthSelected/$count${page?.let { "/$it" } ?: ""}"
                when (val result = handleResponse(currentUrl, transform = transform)) {
                    is Error -> return Error(result.throwable)
                    is Success -> {
                        actionList.addAll(result.result)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return Success(actionList.take(internalLarge))
    }

    class Builder {
        private var nautaSession: NautaSession? = null

        fun nautaSession(session: NautaSession): Builder {
            nautaSession = session
            return this
        }

        private fun createNautaSession(): NautaSession {
            return NautaSessionImpl.builder().build()
        }

        fun build(): UserPortalCommunicator {
            return UserPortalCommunicatorImpl(nautaSession ?: createNautaSession())
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
