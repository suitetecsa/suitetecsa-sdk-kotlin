package cu.suitetecsa.sdk.nauta.framework.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.InetAddress
import java.net.Socket

/**
 * Interfaz que define un método para conectarse a un servidor de sesión utilizando un código de compartición.
 */
interface SessionClient {
    /**
     * Método que se encarga de conectarse a un servidor de sesión utilizando un código de compartición y obtener un mapa de datos de la sesión.
     * @param shareCode Código de compartición que se utilizará para conectarse al servidor de sesión.
     * @return Mapa de datos (`Map<String, String>`) de la sesión.
     */
    fun joinSession(shareCode: String): Map<String, String>
}

/**
 * Clase que implementa la interfaz cu.suitetecsa.sdk.nauta.data.network.SessionClient y se encarga de conectarse a un servidor de sesión utilizando TCP.
 */
class TCPSessionClient : SessionClient {
    /**
     * Método que se encarga de conectarse a un servidor de sesión utilizando TCP y obtener un mapa de datos de la sesión.
     * @param shareCode Código de compartición que se utilizará para conectarse al servidor de sesión.
     * @return Mapa de datos (`Map<String, String>`) de la sesión.
     */
    override fun joinSession(shareCode: String): Map<String, String> {
        // Obtener la dirección IP del dispositivo
        val ipAddress = InetAddress.getLocalHost().hostAddress
        // Construir la dirección IP del servidor utilizando el último octeto del shareCode
        val serverIpAddress = "${ipAddress.substringBeforeLast(".")}.${shareCode.substringBeforeLast("-").toInt()}"
        // Extraer el secreto del shareCode
        val secret = shareCode.substringAfterLast("-")

        // Crear un socket y conectarse al servidor utilizando la dirección IP del servidor y el puerto 8024
        val socket = Socket(serverIpAddress, 8024)
        val outputStream = socket.getOutputStream()
        // Enviar el secreto al servidor a través del socket
        outputStream.write(secret.toByteArray())
        outputStream.flush()

        val inputStream = socket.getInputStream()
        // Leer la respuesta del servidor a través del socket y convertirla en un mapa de datos utilizando la biblioteca de serialización Gson
        val jsonString = inputStream.bufferedReader().readText()
        val data = Gson().fromJson<Map<String, String>>(jsonString, object : TypeToken<Map<String, String>>() {}.type)

        verifySessionData(data)

        // Cerrar el socket y devolver el mapa de datos
        socket.close()
        return data
    }

    /**
     * Método que se encarga de verificar que el mapa de datos de la sesión contiene todas las claves requeridas.
     * @param data Mapa de datos (`Map<String, String>`) de la sesión que se verificará.
     * @throws IllegalArgumentException Si el mapa de datos de la sesión no contiene todas las claves requeridas.
     */
    private fun verifySessionData(data: Map<String, String>) {
        // Definir las claves requeridas para la sesión
        val requiredKeys = setOf("username", "cookies", "wlanuserip", "CSRFHW", "ATTRIBUTE_UUID")
        // Calcular las claves faltantes en el mapa de datos de la sesión
        val missingKeys = requiredKeys.subtract(data.keys)
        // Si hay claves faltantes, lanzar una excepción
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("Missing required keys: $missingKeys")
        }
    }
}