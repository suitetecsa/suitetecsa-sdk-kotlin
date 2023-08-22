package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

/**
 * Interfaz que define un comunicador para interactuar con el portal de usuario.
 * Permite cargar el token CSRF, realizar acciones y transformar las respuestas.
 */
interface UserPortalCommunicator {
    /**
     * Carga el token CSRF desde la URL proporcionada y transforma la respuesta.
     *
     * @param url La URL desde la cual se cargará el token CSRF.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la carga y transformación del token CSRF.
     */
    fun <T> loadCsrf(url: String, transform: (HttpResponse) -> T): ResultType<T>

    /**
     * Realiza una acción en el portal de usuario y transforma la respuesta.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a la respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada.
     */
    fun <T> performAction(action: Action, transform: (HttpResponse) -> T): ResultType<T>

    /**
     * Realiza una acción en el portal de usuario que devuelve una lista y transforma las respuestas.
     *
     * @param action La acción que se va a realizar en el portal.
     * @param transform La función de transformación que se aplicará a cada respuesta del portal.
     * @return Objeto `ResultType` que encapsula el resultado de la acción realizada y transformada en forma de lista.
     */
    fun <T> performListAction(action: Action, transform: (HttpResponse) -> List<T>): ResultType<List<T>>
}
