package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.headers
import cu.suitetecsa.sdk.nauta.core.userAgent
import cu.suitetecsa.sdk.nauta.framework.model.HttpResponse
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import org.jsoup.Connection
import org.jsoup.Jsoup

interface NautaSession {

    // Cookies for the session of the nauta user portal
    val cookies: MutableMap<String, String>

    fun setPortalManager(portalManager: Portal)

    // Get request method
    fun get(
        url: String,
        params: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int? = null
    ): ResultType<HttpResponse>

    // Post request method
    fun post(url: String, data: Map<String, String>): ResultType<HttpResponse>

    // Create a Connection object allowing you to keep the session
    fun connection(
        portalManager: Portal,
        url: String,
        data: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int? = null
    ): Connection {
        var connection = Jsoup.connect(url).ignoreContentType(ignoreContentType)
        if (portalManager == Portal.USER) connection = connection.userAgent(userAgent).headers(headers)
        if (data != null) connection = connection.data(data)
        if (cookies.isNotEmpty()) connection = connection.cookies(cookies)
        if (timeout != null) connection = connection.timeout(timeout)
        return connection
    }
}