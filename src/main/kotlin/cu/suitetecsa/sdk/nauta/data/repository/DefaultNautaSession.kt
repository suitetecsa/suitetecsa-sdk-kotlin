package cu.suitetecsa.sdk.nauta.data.repository

import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.exceptions.OperationException
import cu.suitetecsa.sdk.nauta.core.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.data.model.HttpResponse
import cu.suitetecsa.sdk.nauta.data.model.ResultType
import org.jsoup.Connection

class DefaultNautaSession : NautaSession {
    private val uCookies = mutableMapOf<String, String>()
    private val cCookies = mutableMapOf<String, String>()
    override val userCookies: MutableMap<String, String>
        get() = uCookies
    override val connectCookies: MutableMap<String, String>
        get() = cCookies
    override var csrf: String? = null
    override var userName: String? = null
    override var csrfHw: String? = null
    override var wlanUserIp: String? = null
    override var attributeUUID: String? = null
    override var actionLogin: String? = null
    override var isNautaHome: Boolean = false

    override fun get(
        portalManager: Portal,
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int?
    ): ResultType<HttpResponse> {
        return try {
            val response =
                connection(portalManager, url, params, ignoreContentType, timeout).method(Connection.Method.GET)
                    .execute()
            response.throwExceptionOnFailure(OperationException::class.java, "Fail")
            ResultType.Success(
                HttpResponse(
                    statusCode = response.statusCode(),
                    statusMassage = response.statusMessage(),
                    content = response.bodyAsBytes(),
                    cookies = response.cookies()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }

    override fun post(portalManager: Portal, url: String, data: Map<String, String>): ResultType<HttpResponse> {
        return try {
            val response = connection(portalManager, url, data).method(Connection.Method.POST).execute()
            response.throwExceptionOnFailure(OperationException::class.java, "Fail")
            ResultType.Success(
                HttpResponse(
                    statusCode = response.statusCode(),
                    statusMassage = response.statusMessage(),
                    content = response.bodyAsBytes(),
                    cookies = response.cookies()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ResultType.Error(e)
        }
    }
}