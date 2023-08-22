package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.HttpMethod.GET
import cu.suitetecsa.sdk.nauta.core.HttpMethod.POST
import cu.suitetecsa.sdk.nauta.core.PortalManager
import cu.suitetecsa.sdk.nauta.core.exceptions.NautaAttributeException
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

/**
 * Implementación interna del comunicador con el portal de conexión.
 *
 * @param nautaSession Sesión de conexión con el portal Nauta.
 */
internal class ConnectPortalCommunicatorImpl(
    private val nautaSession: NautaSession
) : ConnectPortalCommunicator {

    /**
     * Realiza una acción en el portal de conexión y transforma la respuesta utilizando la sesión Nauta dada.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    override fun <T> performAction(action: Action, transform: (HttpResponse) -> T): ResultType<T> {
        val url = when (action.portalManager) {
            PortalManager.Connect -> if (action.route.startsWith("/"))
                "${action.portalManager.baseUrl}${action.route}" else action.route
            PortalManager.User -> throw NautaAttributeException("Acción no permitida en el portal de usuario.")
        }

        val response = when (action.method) {
            POST -> nautaSession.post(url, action.data)
            GET -> nautaSession.get(url, action.data)
        }

        return when (response) {
            is ResultType.Error -> ResultType.Error(response.throwable)
            is ResultType.Success -> ResultType.Success(transform(response.result))
        }
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

        fun build(): ConnectPortalCommunicator {
            return ConnectPortalCommunicatorImpl(
                nautaSession ?: createNautaSession()
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}

