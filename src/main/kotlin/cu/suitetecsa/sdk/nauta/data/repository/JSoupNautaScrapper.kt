package cu.suitetecsa.sdk.nauta.data.repository

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.exceptions.*
import cu.suitetecsa.sdk.nauta.core.portalsUrls
import cu.suitetecsa.sdk.nauta.core.urlBase
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.domain.util.parseDateTime
import cu.suitetecsa.sdk.nauta.domain.util.priceStringToFloat
import cu.suitetecsa.sdk.nauta.domain.util.sizeStringToBytes
import cu.suitetecsa.sdk.nauta.domain.util.timeStringToSeconds
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import kotlin.math.ceil

class JSoupNautaScrapper(private val session: NautaSession) : NautaScrapper {
    private val userPortal = Portal.USER
    private val connectPortal = Portal.CONNECT

    private fun actionGet(
        portalManager: Portal, url: String, exc: Class<out Exception>, msg: String, searchHtmlErrors: Boolean = false
    ): Document {
        val response = session.get(portalManager, url)
        response.throwExceptionOnFailure(exc = exc, msg = msg)
        val soup = Jsoup.parse(response.body())
        if (searchHtmlErrors) soup.throwExceptionOnFailure(exc = exc, msg = msg, portalManager = userPortal)
        return soup
    }

    private fun actionPost(
        portalManager: Portal,
        url: String,
        data: Map<String, String>,
        exc: Class<out Exception>,
        msg: String,
        searchHtmlErrors: Boolean = false
    ): Document {
        val response = session.post(portalManager, url, data)
        response.throwExceptionOnFailure(exc = exc, msg = msg)
        val soup = Jsoup.parse(response.body())
        if (searchHtmlErrors) soup.throwExceptionOnFailure(exc = exc, msg = msg, portalManager = userPortal)
        return soup
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
        val soup = soup ?: actionGet(
            portalManager = userPortal,
            url = makeUrl(action, userPortal),
            exc = GetInfoException::class.java,
            msg = "Fail to obtain csrf token",
            searchHtmlErrors = true
        )
        return soup.selectFirst("input[name=csrf]")!!.attr("value")
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
        val large = if (large == 0) count else large

        while (rows.size < large && currentPage >= 1) {
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

    private fun getCurrentYearMonth(): () -> Pair<Int, Int> {
        val currentDate = LocalDate.now()
        var year = currentDate.year
        val month = currentDate.monthValue

        var firstCall = true
        var previousMonth = 0

        return {
            if (firstCall) {
                firstCall = false
                previousMonth = month
                Pair(year, month)
            } else {
                if (previousMonth > 1) previousMonth-- else {
                    previousMonth = 12
                    year--
                }
                Pair(year, previousMonth)
            }
        }
    }

    override val isNautaHome: Boolean
        get() = session.isNautaHome
    override val isLoggedIn: Boolean
        get() = session.isLoggedIn
    override val isUserLoggedIn: Boolean
        get() = session.isUserLoggedIn
    override val isConnected: Boolean
        get() {
            val response = session.get(Portal.CONNECT, makeUrl(Action.CHECK_CONNECTION, Portal.CONNECT))
            return !response.url().toString().contains(connectDomain)
        }

    override fun getDataSession(): Map<String, String> {
        if (!session.isLoggedIn) throw GetInfoException("You are not logged in")
        return mapOf(
            "username" to session.userName!!,
            "CSRFHW" to session.csrfHw!!,
            "wlanuserip" to session.wlanUserIp!!,
            "ATTRIBUTE_UUID" to session.attributeUUID!!
        )
    }

    override fun loadDataSession(dataSession: Map<String, String>) {
        val requiredKeys = setOf("username", "CSRFHW", "wlanuserip", "ATTRIBUTTE_UUID")
        if (!dataSession.keys.containsAll(requiredKeys)) throw LoadInfoException(
            "the keys [\"username\", \"CSRFHW\", \"wlanuserip\", \"ATTRIBUTE_UUID\"] are required"
        )
        session.userName = dataSession["username"]
        session.csrfHw = dataSession["CSRFHW"]
        session.wlanUserIp = dataSession["wlanuserip"]
        session.attributeUUID = dataSession["ATTRIBUTE_UUID"]
    }

    override fun getCaptcha(): ByteArray {
        if (session.csrf.isNullOrEmpty()) {
            val loginResponse = session.get(userPortal, makeUrl(Action.LOGIN, userPortal))
            loginResponse.cookies().forEach { (key, value) -> session.userCookies[key] = value }
            session.csrf = getCsrf(Action.LOGIN, Jsoup.parse(loginResponse.body()))
        }
        val response =
            session.get(
                portalManager = userPortal,
                url = "https://www.portal.nauta.cu/captcha/?",
                ignoreContentType = true,
                timeout = 25000
            )
        response.throwExceptionOnFailure(exc = GetInfoException::class.java, msg = "Fail to obtain captcha image")
        return response.bodyAsBytes()
    }

    override fun init() {
        val landingForm =
            Jsoup.parse(session.get(connectPortal, makeUrl(Action.CHECK_CONNECTION, connectPortal)).body())
                .selectFirst("form[action]")
        val landingData = landingForm?.let { getInputs(it) }
        val formAction = landingForm?.let { it.attr("action") }
        val response = formAction?.let { landingData?.let { it1 -> session.post(connectPortal, it, it1) } }
        response?.cookies()?.forEach { (key, value) -> session.connectCookies[key] = value }
        val loginForm = Jsoup.parse(response?.body() ?: "").selectFirst("form#formulario")
        val loginData = loginForm?.let { getInputs(it) }
        session.csrfHw = loginData?.get("CSRFHW")
        session.wlanUserIp = loginData?.get("wlanuserip")
        if (loginForm != null) {
            session.actionLogin = loginForm.attr("action")
        }
    }

    override fun getInformationConnect(userName: String, password: String): Map<String, Any> {
        if (session.csrfHw.isNullOrEmpty()) init()
        val keys = listOf("account_status", "credit", "expiration_date", "access_areas", "from", "to", "time")
        val accountInfo = mutableMapOf<String, String>()
        val lastsConnections = mutableListOf<MutableMap<String, String>>()
        val soup = Jsoup.parse(
            session.post(
                connectPortal,
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), mapOf(
                    "username" to userName,
                    "password" to password,
                    "wlanuserip" to session.wlanUserIp!!,
                    "CSRFHW" to session.csrfHw!!,
                    "lang" to ""
                )
            ).body()
        )
        for ((index, value) in soup.select("#sessioninfo > tbody > tr > :not(td.key)").withIndex()) {
            accountInfo[keys[index]] = value.text().trim()
        }
        for (tr in soup.select("#sesiontraza > tbody > tr")) {
            val connection = mutableMapOf<String, String>()
            for ((index, value) in tr.select("td").withIndex()) {
                connection[keys[index + 4]] = value.text().trim()
            }
            lastsConnections.add(connection)
        }
        return mapOf(
            "account_info" to accountInfo, "lasts_connections" to lastsConnections
        )
    }

    override fun getRemainingTime(): String {
        return Jsoup.parse(
            session.post(
                connectPortal,
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), mapOf(
                    "op" to "getLeftTime",
                    "ATTRIBUTE_UUID" to session.attributeUUID!!,
                    "CSRFHW" to session.csrfHw!!,
                    "wlanuserip" to session.wlanUserIp!!,
                    "username" to session.userName!!
                )
            ).body()
        ).text().trim()
    }

    override fun getInformationUser(soup: Document?): NautaUser {
        val soup = soup ?: (actionGet(
            userPortal,
            makeUrl(Action.LOAD_USER_INFORMATION, userPortal),
            LoadInfoException::class.java,
            "Fail to obtain the user information",
            true
        ))
        val attrs = soup.selectFirst(".z-depth-1")!!.select(".m6")
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
            if (attrs.size > 7) attrs[10].selectFirst("p")!!.text().trim() else null,
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

    override fun connect(userName: String, password: String) {
        if (session.actionLogin.isNullOrEmpty()) init()
        if (userName.isEmpty() || password.isEmpty()) throw LoginException("username and password are required")
        val response = session.post(connectPortal,
            session.actionLogin!!,
            mapOf(
                "CSRFHW" to session.csrfHw!!,
                "wlanuserip" to session.wlanUserIp!!,
                "username" to userName,
                "password" to password
            )
        )
        session.attributeUUID = Regex("ATTRIBUTE_UUID=(\\w+)&").find(response.body())?.groupValues?.get(1)
        session.userName = userName
    }

    override fun disconnect() {
        val response = session.get(connectPortal,
            makeUrl(Action.LOGOUT, connectPortal), mapOf(
                "username" to session.userName!!,
                "wlanuserip" to session.wlanUserIp!!,
                "CSRFHW" to session.csrfHw!!,
                "ATTRIBUTE_UUID" to session.attributeUUID!!
            )
        )
        if (!response.body().contains("SUCCESS")) throw LogoutException("Fail to logout ${response.body()}")
        session.attributeUUID = null
        session.userName = null
    }

    override fun login(userName: String, password: String, captchaCode: String): NautaUser {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        if (captchaCode.isEmpty()) throw LoginException("The captcha code is required")
        val soup = actionPost(userPortal,
            makeUrl(Action.LOGIN, userPortal), mapOf(
                "csrf" to session.csrf!!,
                "login_user" to userName,
                "password_user" to password,
                "captcha" to captchaCode,
                "btn_submit" to ""
            ), LoginException::class.java, "Fail to login", true
        )
        val userInformation = getInformationUser(soup)
        session.isNautaHome = userInformation.offer != null
        return userInformation
    }

    override fun toUpBalance(rechargeCode: String) {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        actionPost(userPortal,
            makeUrl(Action.RECHARGE, userPortal), mapOf(
                "csrf" to getCsrf(Action.RECHARGE), "recharge_code" to rechargeCode, "btn_submit" to ""
            ), RechargeException::class.java, "Fail to recharge the account balance"
        )
    }

    override fun transferFunds(amount: Float, password: String, destinationAccount: String?) {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val data = mutableMapOf(
            "csrf" to getCsrf(Action.TRANSFER),
            "transfer" to String.format("%.2f", amount).replace(".", ","),
            "password_user" to password,
            "action" to "checkdata"
        )
        if (destinationAccount.isNullOrEmpty() && !session.isNautaHome) {
            throw TransferFundsException("The destination account  is required. Your account are not associated to Nauta Home")
        }
        if (!destinationAccount.isNullOrEmpty()) data["id_cuenta"] = destinationAccount
        actionPost(userPortal,
            makeUrl(Action.TRANSFER, userPortal), data, TransferFundsException::class.java, "Fail to transfer funds"
        )
    }

    override fun changePassword(oldPassword: String, newPassword: String) {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        actionPost(userPortal,
            makeUrl(Action.CHANGE_PASSWORD, userPortal), mapOf(
                "csrf" to getCsrf(Action.CHANGE_PASSWORD),
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ), ChangePasswordException::class.java, "Fail to change the password"
        )
    }

    override fun changeEmailPassword(oldPassword: String, newPassword: String) {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        actionPost(userPortal,
            makeUrl(Action.CHANGE_EMAIL_PASSWORD, userPortal), mapOf(
                "csrf" to getCsrf(Action.CHANGE_EMAIL_PASSWORD),
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ), ChangePasswordException::class.java, "Fail to change the password"
        )
    }

    override fun getConnectionsSummary(year: Int, month: Int): ConnectionsSummary {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (connections, totalTime, totalImport, uploader, downloader, totalTraffic) = getActionsByAction(
            year, month, Action.GET_CONNECTIONS
        )
        return ConnectionsSummary(
            connections.selectFirst("input[name=count]")!!.attr("value").toInt(),
            connections.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            timeStringToSeconds(totalTime.selectFirst(".card-stats-number")!!.text().trim()),
            priceStringToFloat(totalImport.selectFirst(".card-stats-number")!!.text().trim()),
            sizeStringToBytes(uploader.selectFirst(".card-stats-number")!!.text().trim()),
            sizeStringToBytes(downloader.selectFirst(".card-stats-number")!!.text().trim()),
            sizeStringToBytes(totalTraffic.selectFirst(".card-stats-number")!!.text().trim())
        )
    }

    override fun getConnections(
        year: Int, month: Int, summary: ConnectionsSummary?, large: Int, reversed: Boolean
    ): List<Connection> {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getConnectionsSummary(year, month)
        val connections = mutableListOf<Connection>()
        if (summary.count != 0) {
            val rows = getHTMLTableRows(
                Action.GET_CONNECTIONS,
                summary.yearMonthSelected,
                summary.count,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (startSessionTag, endSessionTag, durationTag, uploadedTag, downloadedTag, importTag) = row.select(
                        "td"
                    )
                    connections.add(
                        Connection(
                            parseDateTime(startSessionTag.text().trim()),
                            parseDateTime(endSessionTag.text().trim()),
                            timeStringToSeconds(durationTag.text().trim()),
                            sizeStringToBytes(uploadedTag.text().trim()),
                            sizeStringToBytes(downloadedTag.text().trim()),
                            priceStringToFloat(importTag.text().trim())
                        )
                    )
                }
            }
        }
        return connections
    }

    override fun getRechargesSummary(year: Int, month: Int): RechargesSummary {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (recharges, totalImport) = getActionsByAction(year, month, Action.GET_RECHARGES)
        return RechargesSummary(
            recharges.selectFirst("input[name=count]")!!.attr("value").toInt(),
            recharges.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            priceStringToFloat(totalImport.selectFirst(".card-stats-number")!!.text().trim())
        )
    }

    override fun getRecharges(
        year: Int, month: Int, summary: RechargesSummary?, large: Int, reversed: Boolean
    ): List<Recharge> {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getRechargesSummary(year, month)
        val recharges = mutableListOf<Recharge>()
        if (summary.count != 0) {
            val rows = getHTMLTableRows(
                Action.GET_RECHARGES,
                summary.yearMonthSelected,
                summary.count,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, channelTag, typeTag) = row.select("td")
                    recharges.add(
                        Recharge(
                            parseDateTime(dateTag.text().trim()),
                            priceStringToFloat(importTag.text().trim()),
                            channelTag.text().trim(),
                            typeTag.text().trim()
                        )
                    )
                }
            }
        }
        return recharges
    }

    override fun getTransfersSummary(year: Int, month: Int): TransfersSummary {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (transfers, totalImport) = getActionsByAction(year, month, Action.GET_TRANSFERS)
        return TransfersSummary(
            transfers.selectFirst("input[name=count]")!!.attr("value").toInt(),
            transfers.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            priceStringToFloat(totalImport.selectFirst(".card-stats-number")!!.text().trim())
        )
    }

    override fun getTransfers(
        year: Int, month: Int, summary: TransfersSummary?, large: Int, reversed: Boolean
    ): List<Transfer> {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getTransfersSummary(year, month)
        val transfers = mutableListOf<Transfer>()
        if (summary.count != 0) {
            val rows = getHTMLTableRows(
                Action.GET_TRANSFERS,
                summary.yearMonthSelected,
                summary.count,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, destinyAccountTag) = row.select("td")
                    transfers.add(
                        Transfer(
                            parseDateTime(dateTag.text().trim()),
                            priceStringToFloat(importTag.text().trim()),
                            destinyAccountTag.text().trim()
                        )
                    )
                }
            }
        }
        return transfers
    }

    override fun getQuotesPaidSummary(year: Int, month: Int): QuotesPaidSummary {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (quotesPaid, totalImport) = getActionsByAction(year, month, Action.GET_QUOTES_PAID)
        return QuotesPaidSummary(
            quotesPaid.selectFirst("input[name=count]")!!.attr("value").toInt(),
            quotesPaid.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            priceStringToFloat(totalImport.selectFirst(".card-stats-number")!!.text().trim())
        )
    }

    override fun getQuotesPaid(
        year: Int, month: Int, summary: QuotesPaidSummary?, large: Int, reversed: Boolean
    ): List<QuotePaid> {
        if (!session.isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getQuotesPaidSummary(year, month)
        val quotesPaid = mutableListOf<QuotePaid>()
        if (summary.count != 0) {
            val rows = getHTMLTableRows(
                Action.GET_QUOTES_PAID,
                summary.yearMonthSelected,
                summary.count,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, channelTag, typeTag, officeTag) = row.select("td")
                    quotesPaid.add(
                        QuotePaid(
                            parseDateTime(dateTag.text().trim()),
                            priceStringToFloat(importTag.text().trim()),
                            channelTag.text().trim(),
                            typeTag.text().trim(),
                            officeTag.text().trim()
                        )
                    )
                }
            }
        }
        return quotesPaid
    }

}