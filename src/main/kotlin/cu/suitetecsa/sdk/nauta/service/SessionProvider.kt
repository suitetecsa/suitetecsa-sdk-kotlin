package cu.suitetecsa.sdk.nauta.service

import cu.suitetecsa.sdk.nauta.utils.Portal
import cu.suitetecsa.sdk.nauta.utils.headers
import cu.suitetecsa.sdk.nauta.utils.userAgent
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup

interface SessionProvider {

    val userCookies: MutableMap<String, String>
    val connectCookies: MutableMap<String, String>

    fun get(
        portalManager: Portal,
        url: String,
        params: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int? = null
    ): Response

    fun post(portalManager: Portal, url: String, data: Map<String, String>): Response
    fun connection(
        portalManager: Portal,
        url: String,
        data: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int? = null
    ): Connection {
        val cookies = when (portalManager) {
            Portal.CONNECT -> {
                connectCookies
            }

            Portal.USER -> {
                userCookies
            }
        }
        var connection = Jsoup.connect(url).userAgent(userAgent).headers(headers).ignoreContentType(ignoreContentType)
        if (data != null) connection = connection.data(data)
        if (cookies.isNotEmpty()) connection = connection.cookies(cookies)
        if (timeout != null) connection = connection.timeout(timeout)
        return connection
    }
}