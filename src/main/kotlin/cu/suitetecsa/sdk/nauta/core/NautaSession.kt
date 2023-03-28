package cu.suitetecsa.sdk.nauta.core

import cu.suitetecsa.sdk.nauta.LoginException
import cu.suitetecsa.sdk.nauta.OperationException
import cu.suitetecsa.sdk.nauta.service.SessionProvider
import cu.suitetecsa.sdk.nauta.utils.Portal
import org.jsoup.Connection

class NautaSession : SessionProvider {
    private val uCookies = mutableMapOf<String, String>()
    private val cCookies = mutableMapOf<String, String>()
    override val userCookies: MutableMap<String, String>
        get() = uCookies
    override val connectCookies: MutableMap<String, String>
        get() = cCookies

    override fun get(
        portalManager: Portal,
        url: String,
        params: Map<String, String>?,
        ignoreContentType: Boolean,
        timeout: Int?
    ): Connection.Response {
        val response =
            connection(portalManager, url, params, ignoreContentType, timeout).method(Connection.Method.GET).execute()
        response.throwExceptionOnFailure(OperationException::class.java, "Fail")
        return response
    }

    override fun post(portalManager: Portal, url: String, data: Map<String, String>): Connection.Response {
        val response = connection(portalManager, url, data).method(Connection.Method.POST).execute()
        if (url.contains("LoginServlet")) {
            if (!response.url().toString().contains("online.do")) {
                val soup = response.parse()
                soup.throwExceptionOnFailure(LoginException::class.java, "Fail to login", portalManager)
                throw LoginException("Fail to login")
            }
        }
        response.throwExceptionOnFailure(OperationException::class.java, "Fail")
        return response
    }
}