package cu.suitetecsa.sdk.nauta.data.repository

import cu.suitetecsa.sdk.nauta.data.model.HttpResponse

interface NautaService {
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

    fun get(url: String): String?
    fun get(url: String, verifyResponse: (HttpResponse) -> Unit): String?
    fun post(url: String, data: Map<String, String>? = null): String?
    fun post(url: String, data: Map<String, String>? = null, verifyResponse: (HttpResponse) -> Unit): String?
}