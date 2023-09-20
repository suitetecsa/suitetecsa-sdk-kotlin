package cu.suitetecsa.sdk.network

/**
 * Clase que representa una respuesta HTTP.
 *
 * @param statusCode El código de estado de la respuesta HTTP.
 * @param statusMessage El mensaje de estado de la respuesta HTTP.
 * @param content El contenido de la respuesta HTTP como un arreglo de bytes (opcional).
 * @param cookies Las cookies de la respuesta HTTP (opcional).
 */
class HttpResponse(
    val statusCode: Int,
    val statusMessage: String,
    val content: ByteArray?,
    val cookies: Map<String, String>?
) {
    /**
     * El contenido de la respuesta HTTP como una cadena de texto, decodificado utilizando el conjunto de caracteres UTF-8.
     */
    val text: String?
        get() = this.content?.toString(Charsets.UTF_8)

    /**
     * Compara esta instancia de HttpResponse con otro objeto para determinar si son iguales.
     *
     * @param other El objeto a comparar.
     * @return `true` si los objetos son iguales, `false` en caso contrario.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (statusCode != other.statusCode) return false
        if (statusMessage != other.statusMessage) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) return false

        return true
    }

    /**
     * Calcula el código hash de esta instancia de HttpResponse.
     *
     * @return El código hash calculado.
     */
    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + statusMessage.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        return result
    }
}
