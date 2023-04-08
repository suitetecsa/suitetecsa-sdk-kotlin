package cu.suitetecsa.sdk.nauta.data.model

import java.net.URL

data class HttpResponse(val statusCode: Int, val statusMassage: String, val text: String?, val content: ByteArray?, val url: URL)
