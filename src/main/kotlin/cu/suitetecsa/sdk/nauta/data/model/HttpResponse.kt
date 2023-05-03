package cu.suitetecsa.sdk.nauta.data.model

class HttpResponse(
    val statusCode: Int,
    val statusMassage: String,
    val content: ByteArray?,
    val cookies: Map<String, String>?
) {
    val text: String?
        get() = this.content?.toString(Charsets.UTF_8)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpResponse

        if (statusCode != other.statusCode) return false
        if (statusMassage != other.statusMassage) return false
        if (content != null) {
            if (other.content == null) return false
            if (!content.contentEquals(other.content)) return false
        } else if (other.content != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + statusMassage.hashCode()
        result = 31 * result + (content?.contentHashCode() ?: 0)
        return result
    }
}
