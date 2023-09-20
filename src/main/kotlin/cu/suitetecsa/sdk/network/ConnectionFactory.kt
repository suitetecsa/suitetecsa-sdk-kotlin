package cu.suitetecsa.sdk.network

import org.jsoup.Connection

/**
 * Esta interfaz define una factoría para crear conexiones utilizadas en el proceso de comunicación.
 * Proporciona un método para crear y devolver una conexión utilizando la URL y los datos proporcionados.
 */
interface ConnectionFactory {
    /**
     * Crea y devuelve una conexión utilizando la URL y los datos proporcionados.
     *
     * @param url La URL a la que se va a establecer la conexión.
     * @param requestData Datos para la solicitud (opcional).
     * @param cookies Cookies para la solicitud (opcional).
     * @return Un objeto `Connection` que representa la conexión creada.
     */
    fun createConnection(url: String, requestData: Map<String, String>?, cookies: Map<String, String>?): Connection
}
