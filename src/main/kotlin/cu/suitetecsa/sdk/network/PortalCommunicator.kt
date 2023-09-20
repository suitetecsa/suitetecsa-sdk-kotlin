package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.nauta.domain.model.ResultType

/**
 * Esta interfaz define un comunicador para interactuar con el portal de conexión.
 * Permite realizar acciones y transformar las respuestas utilizando un transformador personalizado.
 */
interface PortalCommunicator {
    /**
     * Realiza una acción en el portal de conexión y transforma la respuesta según la función dada.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    fun <T> performAction(action: Action, transform: (HttpResponse) -> T): ResultType<T>

    /**
     * Realiza una acción en el portal de conexión y transforma la respuesta según la función dada.
     *
     * @param url La URL a la que se va a realizar la solicitud.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    fun <T> performAction(url: String, transform: (HttpResponse) -> T): ResultType<T>
}
