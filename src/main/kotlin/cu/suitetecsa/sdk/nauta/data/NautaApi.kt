package cu.suitetecsa.sdk.nauta.data

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.nauta.core.exceptions.GetInfoException
import cu.suitetecsa.sdk.nauta.core.exceptions.LoadInfoException
import cu.suitetecsa.sdk.nauta.data.model.*
import cu.suitetecsa.sdk.nauta.data.repository.NautaSession
import cu.suitetecsa.sdk.nauta.domain.model.NautaUser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import kotlin.math.ceil

class NautaApi(private val session: NautaSession) {
    private val userPortal = Portal.USER
    private val connectPortal = Portal.CONNECT

    private fun actionGet(
        portalManager: Portal, url: String, exc: Class<out Exception>, msg: String, searchHtmlErrors: Boolean = false
    ): Document {
        val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
        when (val result = session.get(portalManager, url)) {
            is ResultType.Error -> {
                throw exceptionConstructor.newInstance(
                    "$msg :: ${result.error.message}"
                )
            }

            is ResultType.Success -> {
                result.result.text?.let {
                    val soup = Jsoup.parse(it)
                    if (searchHtmlErrors) soup.throwExceptionOnFailure(exc = exc, msg = msg, portalManager = userPortal)
                    return soup
                }
            }
        }
        return Document("")
    }

    private fun actionPost(
        portalManager: Portal,
        url: String,
        data: Map<String, String>,
        exc: Class<out Exception>,
        msg: String,
        searchHtmlErrors: Boolean = false
    ): Document {
        val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
        when (val result = session.post(portalManager, url, data)) {
            is ResultType.Error -> {
                throw exceptionConstructor.newInstance(
                    "$msg :: ${result.error.message}"
                )
            }

            is ResultType.Success -> {
                result.result.text?.let {
                    val soup = Jsoup.parse(it)
                    if (searchHtmlErrors) soup.throwExceptionOnFailure(exc = exc, msg = msg, portalManager = userPortal)
                    return soup
                }
            }
        }
        return Document("")
    }

    private fun makeUrl(
        action: Action,
        portalManager: Portal,
        getAction: Boolean = false,
        subAction: String? = null,
        yearMonthSelected: String? = null,
        count: Int? = null,
        page: Int? = null
    ): String {
        if (action == Action.CHECK_CONNECTION) {
            return portalsUrls[portalManager]!![action] as String
        } else if (!getAction) {
            return "${urlBase[portalManager]}${portalsUrls[portalManager]!![action]}"
        } else {
            val url = "${urlBase[portalManager]}${(portalsUrls[portalManager]!![action]!! as Map<*, *>)[subAction]}"
            when (subAction) {
                "base", "summary" -> {
                    return url
                }

                "list" -> {
                    if (yearMonthSelected.isNullOrEmpty()) {
                        throw Exception("yearMonthSelected is required")
                    }
                    if (count == null) {
                        throw Exception("count is required")
                    }
                    return if (page == null) {
                        "${url}${yearMonthSelected}/${count}"
                    } else "${url}${yearMonthSelected}/${count}/${page}"
                }
            }
        }
        return ""
    }

    private fun getCsrf(action: Action, soup: Document? = null): String {
        return (soup ?: actionGet(
            portalManager = userPortal,
            url = makeUrl(action, userPortal),
            exc = GetInfoException::class.java,
            msg = "Fail to obtain csrf token",
            searchHtmlErrors = true
        )).selectFirst("input[name=csrf]")!!.attr("value")
    }

    private fun getInputs(formSoup: Element): Map<String, String> {
        val inputs = mutableMapOf<String, String>()
        for (input in formSoup.select("input[name]")) {
            inputs[input.attr("name")] = input.attr("value")
        }
        return inputs
    }

    private fun getActionsByAction(year: Int, month: Int, action: Action): Elements {
        val actionsDetailKeys = mapOf(
            Action.GET_CONNECTIONS to "service_detail",
            Action.GET_RECHARGES to "recharge_detail",
            Action.GET_TRANSFERS to "transfer_detail",
            Action.GET_QUOTES_PAID to "nautahogarpaid_detail",
        )
        val errorMessages = mapOf(
            Action.GET_CONNECTIONS to "Fail to obtain connections list",
            Action.GET_RECHARGES to "Fail to obtain recharges list",
            Action.GET_TRANSFERS to "Fail to obtain transfers list",
            Action.GET_QUOTES_PAID to "Fail to obtain quotes paid list",
        )
        val yearMonth = "$year-${String.format("%02d", month)}"
        val soup = actionPost(
            userPortal,
            makeUrl(action, userPortal, true, "summary"), mapOf(
                "csrf" to getCsrf(
                    action, actionGet(
                        userPortal,
                        makeUrl(action, userPortal, true, "base"),
                        GetInfoException::class.java,
                        "Fail to obtain the csrf token",
                        true
                    )
                ), "year_month" to yearMonth, "list_type" to actionsDetailKeys[action]!!
            ), GetInfoException::class.java, errorMessages[action] ?: ""
        )
        return soup.selectFirst("#content")!!.select(".card-content")
    }

    private fun getTableBody(url: String): Element {
        val soup = actionGet(userPortal, url, GetInfoException::class.java, "Fail to obtain information", true)
        return soup.selectFirst(".responsive-table > tbody")!!
    }

    private fun getHTMLTableRows(
        action: Action, yearMonthSelected: String, count: Int, large: Int = 0, reversed: Boolean = false
    ): MutableList<Element> {
        val rows = mutableListOf<Element>()
        val totalPages = ceil(count.toDouble() / 14.0).toInt()
        var currentPage = if (reversed) totalPages else 1
        val internalLarge = if (large == 0) count else large

        while (rows.size < internalLarge && currentPage >= 1) {
            val page = if (currentPage != 1) currentPage else null
            val url = makeUrl(
                action, userPortal, true, "list", yearMonthSelected, count, page
            )
            val tableBody = getTableBody(url)
            tableBody.let {
                val rowsPage = if (reversed) it.select("tr").reversed() else it.select("tr")
                rows.addAll(rowsPage)
            }
            currentPage += if (reversed) -1 else 1
        }
        return rows
    }

    val isNautaHome: Boolean
        get() = session.isNautaHome
    val isLoggedIn: Boolean
        get() = session.isLoggedIn
    val isUserLoggedIn: Boolean
        get() = session.isUserLoggedIn
    val isConnected: Boolean
        get() {
            when (val result = session.get(Portal.CONNECT, makeUrl(Action.CHECK_CONNECTION, Portal.CONNECT))) {
                is ResultType.Error -> {
                    throw result.error
                }

                is ResultType.Success -> {
                    result.result.text?.let { return !it.contains(connectDomain) }
                }
            }
            return false
        }
    val isUnderCaptivePortal: Boolean
        get() {
            return !isConnected
        }

    fun getDataSession(): Map<String, String> {
        if (!session.isLoggedIn) throw GetInfoException("You are not logged in")
        return mapOf(
            "username" to session.userName!!,
            "CSRFHW" to session.csrfHw!!,
            "wlanuserip" to session.wlanUserIp!!,
            "ATTRIBUTE_UUID" to session.attributeUUID!!
        )
    }

    fun loadDataSession(dataSession: Map<String, String>) {
        val requiredKeys = setOf("username", "CSRFHW", "wlanuserip", "ATTRIBUTTE_UUID")
        if (!dataSession.keys.containsAll(requiredKeys)) throw LoadInfoException(
            "the keys [\"username\", \"CSRFHW\", \"wlanuserip\", \"ATTRIBUTE_UUID\"] are required"
        )
        session.userName = dataSession["username"]
        session.csrfHw = dataSession["CSRFHW"]
        session.wlanUserIp = dataSession["wlanuserip"]
        session.attributeUUID = dataSession["ATTRIBUTE_UUID"]
    }

    fun getCaptcha(): ByteArray {
        if (session.csrf.isNullOrEmpty()) {
            when (val loginResult = session.get(userPortal, makeUrl(Action.LOGIN, userPortal))) {
                is ResultType.Error -> {
                    throw loginResult.error
                }

                is ResultType.Success -> {
                    loginResult.result.cookies?.let {
                        it.forEach { (key, value) ->
                            session.userCookies[key] = value
                        }
                    }
                    loginResult.result.text?.let { session.csrf = getCsrf(Action.LOGIN, Jsoup.parse(it)) }
                }
            }
        }

        when (val result = session.get(
            portalManager = userPortal,
            url = "https://www.portal.nauta.cu/captcha/?",
            ignoreContentType = true,
            timeout = 25000
        )) {
            is ResultType.Error -> {
                throw GetInfoException("Fail to obtain captcha image :: ${result.error.message}")
            }

            is ResultType.Success -> {
                return result.result.content ?: byteArrayOf()
            }
        }
    }

    private fun getHomeCaptivePortalResponse(url: String, data: Map<String, String>): HttpResponse {
        when (val result = session.post(connectPortal, url, data)) {
            is ResultType.Error -> {
                throw result.error
            }

            is ResultType.Success -> {
                return result.result
            }
        }
    }

    fun init() {
        when (val result = session.get(connectPortal, makeUrl(Action.CHECK_CONNECTION, connectPortal))) {
            is ResultType.Error -> {
                throw result.error
            }

            is ResultType.Success -> {
                result.result.text?.let {
                    val landingForm = Jsoup.parse(it).body().selectFirst("form[action]")
                    landingForm?.let { landingHtml ->
                        val landingData = getInputs(landingHtml)
                        val formAction = landingHtml.attr("action")
                        val homeResult = getHomeCaptivePortalResponse(formAction, landingData)
                        homeResult.cookies?.forEach { cookie ->
                            session.connectCookies[cookie.key] = cookie.value
                        }
                        homeResult.text?.let { html ->
                            Jsoup.parse(html).body().selectFirst("form.form")?.let { form ->
                                val loginData = getInputs(form)
                                session.wlanUserIp = loginData["wlanuserip"]
                                session.csrfHw = loginData["CSRFHW"]
                                session.actionLogin = form.attr("action")
                            }
                        }
                    }
                }
            }
        }
    }

    fun getInformationConnect(userName: String, password: String): NautaConnectUser {
        if (session.csrfHw.isNullOrEmpty()) init()
        val keys = listOf("account_status", "credit", "expiration_date", "access_areas", "from", "to", "time")
        val lastsConnections = mutableListOf<LastsConnection>()
        when (val result = session.post(
            connectPortal,
            makeUrl(Action.LOAD_USER_INFORMATION, connectPortal),
            mapOf(
                "username" to userName,
                "password" to password,
                "wlanuserip" to session.wlanUserIp!!,
                "CSRFHW" to session.csrfHw!!,
                "lang" to ""
            )
        )) {
            is ResultType.Error -> {
                throw result.error
            }

            is ResultType.Success -> {
                return result.result.text?.let {
                    val soup = Jsoup.parse(it)
                    val info = mutableMapOf<String, String>()
                    for ((index, value) in soup.select("#sessioninfo > tbody > tr > :not(td.key)").withIndex()) {
                        info[keys[index]] = value.text().trim()
                    }
                    val accountInfo = AccountInfo(
                        accessAreas = info["access_areas"]!!,
                        accountStatus = info["account_status"]!!,
                        credit = info["credit"]!!,
                        expirationDate = info["expiration_date"]!!
                    )
                    for (tr in soup.select("#sesiontraza > tbody > tr")) {
                        val connection = mutableMapOf<String, String>()
                        for ((index, value) in tr.select("td").withIndex()) {
                            connection[keys[index + 4]] = value.text().trim()
                        }
                        lastsConnections.add(
                            LastsConnection(
                                from = connection["from"]!!,
                                time = connection["to"]!!,
                                to = connection["time"]!!
                            )
                        )
                    }
                    NautaConnectUser(accountInfo = accountInfo, lastsConnections = lastsConnections)
                } ?: NautaConnectUser(
                    accountInfo = AccountInfo(
                        accessAreas = "",
                        accountStatus = "",
                        credit = "",
                        expirationDate = ""
                    ), lastsConnections = listOf()
                )
            }
        }
    }

    fun getRemainingTime(): String {

        when (
            val timeResult = session.post(
                connectPortal,
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal),
                mapOf(
                    "op" to "getLeftTime",
                    "ATTRIBUTE_UUID" to session.attributeUUID!!,
                    "CSRFHW" to session.csrfHw!!,
                    "wlanuserip" to session.wlanUserIp!!,
                    "username" to session.userName!!
                )
            )
        ) {
            is ResultType.Error -> {
                throw timeResult.error
            }

            is ResultType.Success -> {
                return timeResult.result.text?.let { Jsoup.parse(it).text().trim() } ?: "Hubo un error"
            }
        }
    }

    fun getInformationUser(soup: Document?): NautaUser {
        val attrs = (soup ?: (actionGet(
            userPortal,
            makeUrl(Action.LOAD_USER_INFORMATION, userPortal),
            LoadInfoException::class.java,
            "Fail to obtain the user information",
            true
        ))).selectFirst(".z-depth-1")!!.select(".m6")
        return NautaUser(
            attrs[0].selectFirst("p")!!.text().trim(),
            attrs[1].selectFirst("p")!!.text().trim(),
            attrs[2].selectFirst("p")!!.text().trim(),
            attrs[3].selectFirst("p")!!.text().trim(),
            attrs[4].selectFirst("p")!!.text().trim(),
            attrs[5].selectFirst("p")!!.text().trim(),
            attrs[6].selectFirst("p")!!.text().trim(),
            attrs[7].selectFirst("p")!!.text().trim(),
            if (attrs.size > 8) attrs[8].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[9].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[10].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[11].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[12].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[13].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[14].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[15].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[16].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[17].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[18].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[19].selectFirst("p")!!.text().trim() else null,
            if (attrs.size > 8) attrs[20].selectFirst("p")!!.text().trim() else null
        )
    }
}