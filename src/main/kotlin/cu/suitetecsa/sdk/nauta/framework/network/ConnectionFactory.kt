package cu.suitetecsa.sdk.nauta.framework.network

import org.jsoup.Connection

/**
 * Interfaz que define una factoría para crear conexiones utilizadas en el proceso de comunicación.
 */
interface ConnectionFactory {
    /**
     * Crea y devuelve una conexión utilizando la URL y los datos proporcionados.
     *
     * @param url La URL a la que se va a establecer la conexión.
     * @param requestData Datos para la solicitud (opcional).
     * @return Objeto `Connection` que representa la conexión creada.
     */
    fun createConnection(url: String, requestData: Map<String, String>?): Connection
}
