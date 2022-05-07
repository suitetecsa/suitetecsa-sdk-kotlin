package cu.suitetecsa.kotlibsuitetecsa

import org.jsoup.Connection
import org.jsoup.Jsoup

interface Session {
    val userAgent: String
        get() = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:97.0) Gecko/20100101 Firefox/97.0"
    val headers: MutableMap<String, String>
        get() = mutableMapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "es-MX,es;q=0.8,en-US;q=0.5,en;q=0.3"
        )
    var cookies: MutableMap<String, String>

    fun connect(url: String, data: Map<String, String>? = null): Connection {
        var connection = Jsoup.connect(url).userAgent(userAgent).headers(headers)
        if (data != null) connection = connection.data(data)
        if (cookies.isNotEmpty()) connection = connection.cookies(cookies)
        return connection
    }

    fun getCookies(url: String): MutableMap<String, String> {
        return connect(url).execute().cookies()
    }
}