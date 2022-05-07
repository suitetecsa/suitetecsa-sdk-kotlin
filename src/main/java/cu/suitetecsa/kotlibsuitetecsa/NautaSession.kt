package cu.suitetecsa.kotlibsuitetecsa

import org.jsoup.Connection
import org.jsoup.nodes.Element

class NautaSession(override var cookies: MutableMap<String, String> = mutableMapOf()) : Session {

    private val checkPage = "http://www.cubadebate.cu"
    private val loginDomain = "secure.etecsa.net"

    private var actionLogin: String? = null

    var csrfHW: String? = null
    var wlanUserIP: String? = null
    var attributeUUID: String? = null

    private fun getInputs(formSoup: Element): Map<String, String> {
        val inputs = mutableMapOf<String, String>()
        for (input in formSoup.select("input[name]")) {
            inputs[input.attr("name")] = input.attr("value")
        }
        return inputs
    }

    private fun isConnected(): Boolean {
        val r = connect(checkPage).execute()
        return r.url().toString().contains(loginDomain)
    }

    fun init() {
        if (isConnected()) println("Estas conectado")
        var soup = connect(checkPage).get()
        var formSoup = soup.selectFirst("form")
        var data = formSoup?.let { getInputs(it) }
        actionLogin = formSoup?.attr("action")

        val response = actionLogin?.let { connect(it, data).method(Connection.Method.POST).execute() }
        if (response != null) {
            cookies = response.cookies()
            soup = response.parse()
            formSoup = soup.selectFirst("form[id=\"formulario\"]")
            data = formSoup?.let { getInputs(it) }
            actionLogin = formSoup?.attr("action")
            if (data != null) {
                csrfHW = data["CSRFHW"]
                wlanUserIP = data["wlanuserip"]
            }
        }
    }

    fun login(userName: String, password: String, cookies: MutableMap<String, String>) {
        this.cookies = cookies
        val response = connect(
            actionLogin!!,
            mapOf(
                "CSRFHW" to csrfHW!!,
                "wlanuserip" to wlanUserIP!!,
                "username" to userName,
                "password" to password
            )
        ).method(Connection.Method.POST).execute()
        val soup = response.parse()
        var str = ""
        for (wholeData in soup.getElementsByTag("script").first()?.dataNodes()!!) {
            str = wholeData.wholeData
        }
        attributeUUID = Regex(pattern = """ATTRIBUTE_UUID=(?<attr>\w+)&CSRFHW=""")
            .find(input = str)?.groups?.get("attr")?.value
    }

    fun logout(userName: String, cookies: MutableMap<String, String>) {
        val response = connect(
            "https://secure.etecsa.net:8443/LogoutServlet?",
            mapOf(
                "CSRFHW" to csrfHW!!,
                "username" to userName,
                "ATTRIBUTE_UUID" to attributeUUID!!,
                "wlanuserip" to wlanUserIP!!
            )
        ).execute()
        println(response.parse())
    }

    fun getUserTime(userName: String, cookies: MutableMap<String, String>): String {
        this.cookies = cookies
        val response = connect(
            "https://secure.etecsa.net:8443/EtecsaQueryServlet",
            mapOf(
                "op" to "getLeftTime",
                "ATTRIBUTE_UUID" to attributeUUID!!,
                "CSRFHW" to csrfHW!!,
                "wlanuserip" to wlanUserIP!!,
                "username" to userName
            )
        ).method(Connection.Method.POST).execute()
        return response.parse().text().trim()
    }
}