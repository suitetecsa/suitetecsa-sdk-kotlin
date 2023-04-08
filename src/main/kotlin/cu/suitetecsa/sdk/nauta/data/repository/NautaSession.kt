package cu.suitetecsa.sdk.nauta.data.repository

import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.headers
import cu.suitetecsa.sdk.nauta.core.userAgent
import org.jsoup.Connection
import org.jsoup.Connection.Response
import org.jsoup.Jsoup

interface NautaSession {

    // Cookies for the session of the nauta user portal
    val userCookies: MutableMap<String, String>
    // Cookies for de nauta captive portal session
    val connectCookies: MutableMap<String, String>

    // Session attribute for the nauta user portal
    var csrf: String?
    // Session attributes for de nauta's captive portal
    var userName: String?
    var csrfHw: String?
    var wlanUserIp: String?
    var attributeUUID: String?
    var actionLogin: String?

    // Variables for the nauta captive portal session
    val isLoggedIn: Boolean
        get() {
            return !attributeUUID.isNullOrEmpty()
        }

    // Variables for the nauta user portal session
    val isUserSessionInitialized: Boolean
        get() {
            return !csrf.isNullOrEmpty()
        }
    val isUserLoggedIn: Boolean
        get() {
            return !userName.isNullOrEmpty()
        }
    var isNautaHome: Boolean

    // Get request method
    fun get(
        portalManager: Portal,
        url: String,
        params: Map<String, String>? = null,
        ignoreContentType: Boolean = false,
        timeout: Int? = null
    ): Response

    // Post request method
    fun post(portalManager: Portal, url: String, data: Map<String, String>): Response

    // Create a Connection object allowing you to keep the session
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
        var connection = Jsoup.connect(url).ignoreContentType(ignoreContentType)
        if (portalManager == Portal.USER) connection = connection.userAgent(userAgent).headers(headers)
        if (data != null) connection = connection.data(data)
        if (cookies.isNotEmpty()) connection = connection.cookies(cookies)
        if (timeout != null) connection = connection.timeout(timeout)
        return connection
    }
}