package cu.suitetecsa.kotlibsuitetecsa

import org.jsoup.Connection
import org.jsoup.Jsoup

interface Session {

    /*
        Crea una sesion que maneja las conexiones al portal y las cookies
        devuelta por este.
     */

    // Se usa un user-agent y headers personalizados ya que el portal asi lo requiere
    val userAgent: String
        get() = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:97.0) Gecko/20100101 Firefox/97.0"
    val headers: MutableMap<String, String>
        get() = mutableMapOf(
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
            "Accept-Encoding" to "gzip, deflate, br",
            "Accept-Language" to "es-MX,es;q=0.8,en-US;q=0.5,en;q=0.3"
        )

    // Se define una variable para almacenar las cookies devueltas por el portal
    var cookies: MutableMap<String, String>

    // Crea una conexion pasando como parametros la informacion necesaria
    fun connect(url: String, data: Map<String, String>? = null): Connection {
        var connection = Jsoup.connect(url).userAgent(userAgent).headers(headers)
        if (data != null) connection = connection.data(data)
        if (cookies.isNotEmpty()) connection = connection.cookies(cookies)
        return connection
    }

    // Obtine las cookies del portal correspondiente
    fun getCookies(url: String): MutableMap<String, String> {
        return connect(url).execute().cookies()
    }
}