package cu.suitetecsa.sdk.nauta.util

import com.google.gson.Gson
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.SocketTimeoutException

/**
 * Interfaz que define un método para compartir una sesión con otros dispositivos a través de un servidor de sesión.
 */
interface SessionServer {
    /**
     * Método que se encarga de compartir una sesión con otros dispositivos a través de un servidor de sesión.
     * @param data Mapa de datos (`Map<String, String>`) de la sesión que se compartirá.
     * @return Par (`Pair<Thread, String>`) que contiene un objeto Thread que representa el servidor de sesión y el código de compartición de la sesión.
     */
    fun shareSession(data: Map<String, String>): Pair<Thread, String>
}

/**
 * Clase que implementa la interfaz cu.suitetecsa.sdk.nauta.data.network.SessionServer y se encarga de compartir una sesión con otros dispositivos a través de un servidor de sesión TCP.
 */
class TCPSessionServer : SessionServer {

    /**
     * Método que se encarga de compartir una sesión con otros dispositivos a través de un servidor de sesión TCP.
     * @param data Mapa de datos (`Map<String, String>`) de la sesión que se compartirá.
     * @return Par (`Pair<Thread, String>`) que contiene un objeto Thread que representa el servidor de sesión y el código de compartición de la sesión.
     */
    override fun shareSession(data: Map<String, String>): Pair<Thread, String> {
        verifySessionData(data)

        // Generar un secreto aleatorio para la sesión
        val secret = generateSecret()
        // Obtener la dirección IP del dispositivo
        val ipAddress = getDeviceIpAddress()

        // Construir el código de compartición de la sesión utilizando la dirección IP del dispositivo y el secreto
        val shareCode = "${ipAddress.substringAfterLast(".").padStart(3, '0')}-$secret"
        // Crear un objeto Thread que represente el servidor de sesión y ejecutarlo en un hilo separado
        val serverThread = Thread { runServer(data, secret) }
        serverThread.start()

        return Pair(serverThread, shareCode)
    }

    /**
     * Método que se encarga de ejecutar el servidor de sesión TCP.
     * @param data Mapa de datos (`Map<String, String>`) de la sesión que se compartirá.
     * @param secret Secreto utilizado para autenticar la sesión.
     * @param timeout Tiempo máximo de espera para establecer una conexión con el cliente (en segundos).
     */
    private fun runServer(data: Map<String, String>, secret: String, timeout: Int = 30) {
        // Crear un objeto ServerSocket que escuche en el puerto 8024
        val serverSocket = ServerSocket(8024)
        // Establecer el tiempo máximo de espera para establecer una conexión con el cliente
        serverSocket.soTimeout = timeout * 1000

        try {
            println("Listening on port 8024...")
            // Esperar a que se establezca una conexión con un cliente
            val clientSocket = serverSocket.accept()
            println("Connection established from ${clientSocket.inetAddress.hostAddress}")

            // Leer el código de compartición enviado por el cliente
            val receivedShareCode = clientSocket.getInputStream().bufferedReader().readLine()
            println("receivedShareCode: $receivedShareCode")
            if (receivedShareCode == secret) {
                // Si el código de compartición es correcto, enviar el mapa de datos de la sesión al cliente
                val outputStream = clientSocket.getOutputStream()
                println("estamos cerca")
                val json = Gson().toJson(data)
                println("respuesta preparada")
                outputStream.write(json.toByteArray())
                println("respuesta enviada")
                outputStream.flush()
                println("no se que pasa aqui")
                outputStream.close()
                println("Connection closed.")
            } else {
                // Si el código de compartición es incorrecto, cerrar la conexión con el cliente
                clientSocket.getOutputStream().write("Invalid secret".toByteArray())
                clientSocket.close()
            }
        } catch (e: SocketTimeoutException) {
            println("Timeout waiting for connection.")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Cerrar el objeto ServerSocket
            serverSocket.close()
        }
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

    /**
     * Método que se encarga de generar un secreto aleatorio para autenticar la sesión.
     * @return Secreto aleatorio generado.
     */
    private fun generateSecret(): String {
        // Definir los caracteres permitidos para el secreto
        val chars = ('A'..'Z') + ('0'..'9')
        // Generar un secreto de 4 caracteres aleatorios
        return (1..4).map { chars.random() }.joinToString("")
    }

    /**
     * Método que se encarga de obtener la dirección IP del dispositivo.
     * @return Dirección IP del dispositivo.
     * @throws IllegalStateException Si no se puede obtener la dirección IP del dispositivo.
     */
    private fun getDeviceIpAddress(): String {
        // Obtener la lista de interfaces de red del dispositivo y sus direcciones IP
        return NetworkInterface.getNetworkInterfaces().toList().flatMap { it.inetAddresses.toList() }
            // Seleccionar la primera dirección IP que no sea de enlace local ni de bucle invertido y sea de tipo IPv4
            .find { !it.isLinkLocalAddress && !it.isLoopbackAddress && it is Inet4Address }?.hostAddress
            ?: throw IllegalStateException("Unable to get device IP address")
    }
}