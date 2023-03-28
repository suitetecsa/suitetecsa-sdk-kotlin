package cu.suitetecsa.sdk.nauta

import cu.suitetecsa.sdk.nauta.core.component6
import cu.suitetecsa.sdk.nauta.core.throwExceptionOnFailure
import cu.suitetecsa.sdk.nauta.service.NautaProvider
import cu.suitetecsa.sdk.nauta.service.SessionProvider
import cu.suitetecsa.sdk.nauta.utils.Action
import cu.suitetecsa.sdk.nauta.utils.Portal
import cu.suitetecsa.sdk.nauta.utils.portalsUrls
import cu.suitetecsa.sdk.nauta.utils.urlBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.time.LocalDate
import kotlin.math.ceil

class NautaClient(private val nautaSession: SessionProvider) : NautaProvider {
    private var userName: String? = null
    private var password: String? = null
    private var csrf: String? = null
    private var csrfHw: String? = null
    private var wlanUserIp: String? = null
    private var attributeUUID: String? = null
    private var actionLogin: String? = null
    private val isLoggedIn: Boolean
        get() {
            return !attributeUUID.isNullOrEmpty()
        }
    private val isUserSessionInitialized: Boolean
        get() {
            return !csrf.isNullOrEmpty()
        }
    var isNautaHome: Boolean = false
    private val userPortal = Portal.USER
    private val connectPortal = Portal.CONNECT

    private fun actionGet(
        portalManager: Portal, url: String, exc: Class<out Exception>, msg: String, searchHtmlErrors: Boolean = false
    ): Document {
        val response = nautaSession.get(portalManager, url)
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
        val response = nautaSession.post(portalManager, url, data)
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

    override fun getCredentials(): Pair<String, String> {
        return Pair(userName ?: "", password ?: "")
    }

    override fun setCredentials(userName: String, password: String) {
        if (!userName.endsWith("@nauta.com.cu") || userName.endsWith("@nauta.co.cu"))
            throw LoadInfoException("Enter a valid username")
        if (password.length < 8) throw LoadInfoException("Enter a valid password")
        this.userName = userName
        this.password = password
    }

    override fun getDataSession(): Map<String, String> {
        if (!isLoggedIn) throw GetInfoException("You are not logged in")
        return mapOf(
            "username" to userName!!,
            "CSRFHW" to csrfHw!!,
            "wlanuserip" to wlanUserIp!!,
            "ATTRIBUTTE_UUID" to attributeUUID!!
        )
    }

    override fun loadDataSession(dataSession: Map<String, String>) {
        val requiredKeys = setOf("username", "CSRFHW", "wlanuserip", "ATTRIBUTTE_UUID")
        if (!dataSession.keys.containsAll(requiredKeys)) throw LoadInfoException(
            "the keys [\"username\", \"CSRFHW\", \"wlanuserip\", \"ATTRIBUTTE_UUID\"] are required"
        )
        userName = dataSession["username"]
        csrfHw = dataSession["CSRFHW"]
        wlanUserIp = dataSession["wlanuserip"]
        attributeUUID = dataSession["ATTRIBUTTE_UUID"]
    }

    override fun getCaptcha(): ByteArray {
        if (csrf.isNullOrEmpty()) {
            val loginResponse = nautaSession.get(userPortal, makeUrl(Action.LOGIN, userPortal))
            loginResponse.cookies().forEach { (key, value) -> nautaSession.userCookies[key] = value }
            csrf = getCsrf(Action.LOGIN, Jsoup.parse(loginResponse.body()))
        }
        val response =
            nautaSession.get(
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
            Jsoup.parse(nautaSession.get(connectPortal, makeUrl(Action.CHECK_CONNECTION, connectPortal)).body())
                .selectFirst("form[action]")
        val landingData = landingForm?.let { getInputs(it) }
        val formAction = landingForm?.let { it.attr("action") }
        val response = formAction?.let { landingData?.let { it1 -> nautaSession.post(connectPortal, it, it1) } }
        response?.cookies()?.forEach { (key, value) -> nautaSession.connectCookies[key] = value }
        val loginForm = Jsoup.parse(response?.body() ?: "").selectFirst("form#formulario")
        val loginData = loginForm?.let { getInputs(it) }
        csrfHw = loginData?.get("CSRFHW")
        wlanUserIp = loginData?.get("wlanuserip")
        if (loginForm != null) {
            actionLogin = loginForm.attr("action")
        }
    }

    override fun getInformationConnect(): Map<String, Any> {
        if (csrfHw.isNullOrEmpty()) init()
        val keys = listOf("account_status", "credit", "expiration_date", "access_areas", "from", "to", "time")
        val accountInfo = mutableMapOf<String, String>()
        val lastsConnections = mutableListOf<MutableMap<String, String>>()
        val soup = Jsoup.parse(
            nautaSession.post(
                connectPortal,
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), mapOf(
                    "username" to userName!!,
                    "password" to password!!,
                    "wlanuserip" to wlanUserIp!!,
                    "CSRFHW" to csrfHw!!,
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
            nautaSession.post(
                connectPortal,
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), mapOf(
                    "op" to "getLeftTime",
                    "ATTRIBUTE_UUID" to attributeUUID!!,
                    "CSRFHW" to csrfHw!!,
                    "wlanuserip" to wlanUserIp!!,
                    "username" to userName!!
                )
            ).body()
        ).text().trim()
    }

    override fun getInformationUser(soup: Document?): Map<String, Map<String, String>> {
        val keys = listOf(
            "username",
            "blocking_date",
            "date_of_elimination",
            "account_type",
            "service_type",
            "credit",
            "time",
            "mail_account",
            "offer",
            "monthly_fee",
            "download_speeds",
            "upload_speeds",
            "phone",
            "link_identifiers",
            "link_status",
            "activation_date",
            "blocking_date_home",
            "date_of_elimination_home",
            "quote_paid",
            "voucher",
            "debt"
        )
        val userInfo = mutableMapOf<String, String>()
        val soup = soup ?: (actionGet(userPortal,
            makeUrl(Action.LOAD_USER_INFORMATION, userPortal),
            LoadInfoException::class.java,
            "Fail to obtain the user information",
            true
        ))
        for ((index, attr) in soup.selectFirst(".z-depth-1")!!.select(".m6").withIndex()) {
            userInfo[keys[index]] = attr.selectFirst("p")!!.text().trim()
        }
        return mapOf("user_info" to userInfo)
    }

    override fun connect() {
        if (actionLogin.isNullOrEmpty()) init()
        if (userName.isNullOrEmpty() || password.isNullOrEmpty()) throw LoginException("username and password are required")
        val soup = Jsoup.parse(
            nautaSession.post(connectPortal,
                actionLogin!!,
                mapOf(
                    "CSRFHW" to csrfHw!!,
                    "wlanuserip" to wlanUserIp!!,
                    "username" to userName!!,
                    "password" to password!!
                )
            ).body()
        )
        var str = ""
        for (wholeData in soup.getElementsByTag("script").first()?.dataNodes()!!) {
            str = wholeData.wholeData
        }
        attributeUUID =
            Regex(pattern = """ATTRIBUTE_UUID=(?<attr>\w+)&CSRFHW=""").find(input = str)?.groups?.get("attr")?.value.toString()
    }

    override fun disconnect() {
        val response = nautaSession!!.get(connectPortal,
            makeUrl(Action.LOGOUT, connectPortal), mapOf(
                "username" to userName!!,
                "wlanuserip" to wlanUserIp!!,
                "CSRFHW" to csrfHw!!,
                "ATTRIBUTE_UUID" to attributeUUID!!
            )
        )
        if (!response.body().contains("SUCCESS")) throw LogoutException("Fail to logout ${response.body()}")
        attributeUUID = null
    }

    override fun login(captchaCode: String): Map<String, Map<String, String>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        if (captchaCode.isNullOrEmpty()) throw LoginException("The captcha code is required")
        val soup = actionPost(userPortal,
            makeUrl(Action.LOGIN, userPortal), mapOf(
                "csrf" to csrf!!,
                "login_user" to userName!!,
                "password_user" to password!!,
                "captcha" to captchaCode,
                "btn_submit" to ""
            ), LoginException::class.java, "Fail to login", true
        )
        val userInformation = getInformationUser(soup)
        isNautaHome = userInformation.containsKey("offer")
        return userInformation
    }

    override fun toUpBalance(rechargeCode: String) {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        actionPost(userPortal,
            makeUrl(Action.RECHARGE, userPortal), mapOf(
                "csrf" to getCsrf(Action.RECHARGE), "recharge_code" to rechargeCode, "btn_submit" to ""
            ), RechargeException::class.java, "Fail to recharge the account balance"
        )
    }

    override fun transferFunds(amount: Float, destinationAccount: String?) {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val data = mutableMapOf(
            "csrf" to getCsrf(Action.TRANSFER),
            "transfer" to String.format("%.2f", amount).replace(".", ","),
            "password_user" to password!!,
            "action" to "checkdata"
        )
        if (destinationAccount.isNullOrEmpty() && !isNautaHome) {
            throw TransferFundsException("The destination account  is required. Your account are not associated to Nauta Home")
        }
        if (!destinationAccount.isNullOrEmpty()) data["id_cuenta"] = destinationAccount
        actionPost(userPortal,
            makeUrl(Action.TRANSFER, userPortal), data, TransferFundsException::class.java, "Fail to transfer funds"
        )
    }

    override fun changePassword(newPassword: String) {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        actionPost(userPortal,
            makeUrl(Action.CHANGE_PASSWORD, userPortal), mapOf(
                "csrf" to getCsrf(Action.CHANGE_PASSWORD),
                "old_password" to password!!,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ), ChangePasswordException::class.java, "Fail to change the password"
        )
    }

    override fun changeEmailPassword(oldPassword: String, newPassword: String) {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
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

    override fun getConnectionsSummary(year: Int, month: Int): Map<String, Map<String, Any>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (connections, totalTime, totalImport, uploader, downloader, totalTraffic) = getActionsByAction(
            year, month, Action.GET_CONNECTIONS
        )
        val summary = mapOf(
            "count" to connections.selectFirst("input[name=count]")!!.attr("value").toInt(),
            "year_month_selected" to connections.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            "total_time" to totalTime.selectFirst(".card-stats-number")!!.text().trim(),
            "total_import" to totalImport.selectFirst(".card-stats-number")!!.text().trim(),
            "uploaded" to uploader.selectFirst(".card-stats-number")!!.text().trim(),
            "downloaded" to downloader.selectFirst(".card-stats-number")!!.text().trim(),
            "total_time" to totalTraffic.selectFirst(".card-stats-number")!!.text().trim()
        )
        return mapOf(
            "connections_summary" to summary
        )
    }

    override fun getConnections(
        year: Int, month: Int, summary: Map<String, Any>?, large: Int, reversed: Boolean
    ): Map<String, Any> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getConnectionsSummary(year, month)["connections_summary"]!!
        val connections = mutableListOf<Map<String, String>>()
        if (summary["count"]!! != 0) {
            val rows = getHTMLTableRows(
                Action.GET_CONNECTIONS,
                summary["year_month_selected"]!! as String,
                summary["count"]!! as Int,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (startSessionTag, endSessionTag, durationTag, uploadedTag, downloadedTag, importTag) = row.select(
                        "td"
                    )
                    connections.add(
                        mapOf(
                            "start_session" to startSessionTag.text().trim(),
                            "end_session" to endSessionTag.text().trim(),
                            "duration" to durationTag.text().trim(),
                            "uploaded" to uploadedTag.text().trim(),
                            "downloaded" to downloadedTag.text().trim(),
                            "import" to importTag.text().trim()
                        )
                    )
                }
            }
        }
        return mapOf(
            "connections_summary" to summary, "connections" to connections
        )
    }

    override fun getRechargesSummary(year: Int, month: Int): Map<String, Map<String, Any>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (recharges, totalImport) = getActionsByAction(year, month, Action.GET_RECHARGES)
        val summary = mapOf(
            "count" to recharges.selectFirst("input[name=count]")!!.attr("value").toInt(),
            "year_month_selected" to recharges.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            "total_import" to totalImport.selectFirst(".card-stats-number")!!.text().trim()
        )
        return mapOf(
            "recharges_summary" to summary
        )
    }

    override fun getRecharges(
        year: Int, month: Int, summary: Map<String, Any>?, large: Int, reversed: Boolean
    ): Map<String, Any> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getRechargesSummary(year, month)["recharges_summary"]!!
        val recharges = mutableListOf<Map<String, String>>()
        if (summary["count"]!! != 0) {
            val rows = getHTMLTableRows(
                Action.GET_RECHARGES,
                summary["year_month_selected"]!! as String,
                summary["count"]!! as Int,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, channelTag, typeTag) = row.select("td")
                    recharges.add(
                        mapOf(
                            "date" to dateTag.text().trim(),
                            "import" to importTag.text().trim(),
                            "channel" to channelTag.text().trim(),
                            "type" to typeTag.text().trim()
                        )
                    )
                }
            }
        }
        return mapOf(
            "recharges_summary" to summary, "rechargeS" to recharges
        )
    }

    override fun getTransfersSummary(year: Int, month: Int): Map<String, Map<String, Any>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (transfers, totalImport) = getActionsByAction(year, month, Action.GET_TRANSFERS)
        val summary = mapOf(
            "count" to transfers.selectFirst("input[name=count]")!!.attr("value").toInt(),
            "year_month_selected" to transfers.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            "total_import" to totalImport.selectFirst(".card-stats-number")!!.text().trim()
        )
        return mapOf(
            "transfers_summary" to summary
        )
    }

    override fun getTransfers(
        year: Int, month: Int, summary: Map<String, Any>?, large: Int, reversed: Boolean
    ): Map<String, Any> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getTransfersSummary(year, month)["transfers_summary"]!!
        val recharges = mutableListOf<Map<String, String>>()
        if (summary["count"]!! != 0) {
            val rows = getHTMLTableRows(
                Action.GET_TRANSFERS,
                summary["year_month_selected"]!! as String,
                summary["count"]!! as Int,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, destinyAccountTag) = row.select("td")
                    recharges.add(
                        mapOf(
                            "date" to dateTag.text().trim(),
                            "import" to importTag.text().trim(),
                            "destiny_account" to destinyAccountTag.text().trim()
                        )
                    )
                }
            }
        }
        return mapOf(
            "transfers_summary" to summary, "transfers" to recharges
        )
    }

    override fun getQuotesPaidSummary(year: Int, month: Int): Map<String, Map<String, Any>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val (quotesPaid, totalImport) = getActionsByAction(year, month, Action.GET_QUOTES_PAID)
        val summary = mapOf(
            "count" to quotesPaid.selectFirst("input[name=count]")!!.attr("value").toInt(),
            "year_month_selected" to quotesPaid.selectFirst("input[name=year_month_selected]")!!.attr("value"),
            "total_import" to totalImport.selectFirst(".card-stats-number")!!.text().trim()
        )
        return mapOf(
            "quotes_paid_summary" to summary
        )
    }

    override fun getQuotesPaid(
        year: Int, month: Int, summary: Map<String, Any>?, large: Int, reversed: Boolean
    ): Map<String, Any> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val summary = summary ?: getQuotesPaidSummary(year, month)["quotes_paid_summary"]!!
        val recharges = mutableListOf<Map<String, String>>()
        if (summary["count"]!! != 0) {
            val rows = getHTMLTableRows(
                Action.GET_QUOTES_PAID,
                summary["year_month_selected"]!! as String,
                summary["count"]!! as Int,
                large,
                reversed
            )
            if (rows.isNotEmpty()) {
                for (row in rows) {
                    val (dateTag, importTag, channelTag, typeTag, officeTag) = row.select("td")
                    recharges.add(
                        mapOf(
                            "date" to dateTag.text().trim(),
                            "import" to importTag.text().trim(),
                            "channel" to channelTag.text().trim(),
                            "type" to typeTag.text().trim(),
                            "office" to officeTag.text().trim()
                        )
                    )
                }
            }
        }
        return mapOf(
            "quotes_paid_summary" to summary, "quotes_paid" to recharges
        )
    }

    override fun getLasts(action: Action, large: Int): Map<String, MutableList<Map<String, Any>>> {
        if (!isUserSessionInitialized) throw LoginException("The session has not been initialized")
        val yearMonth = getCurrentYearMonth()
        val actionKeys = mapOf(
            Action.GET_CONNECTIONS to "connections",
            Action.GET_RECHARGES to "recharges",
            Action.GET_TRANSFERS to "transfers",
            Action.GET_QUOTES_PAID to "quotes_paid"
        )
        var totalCount = 0
        var retrievedCount = 0
        val actions = mutableListOf<Map<String, Any>>()
        val summaries = mutableListOf<Map<String, Any>>()

        while (totalCount < large) {
            val (year, month) = yearMonth()
            val currentSummary = when (action) {
                Action.GET_CONNECTIONS -> {
                    getConnectionsSummary(year, month)["connections_summary"]
                }

                Action.GET_RECHARGES -> {
                    getRechargesSummary(year, month)["recharges_summary"]
                }

                Action.GET_TRANSFERS -> {
                    getTransfersSummary(year, month)["transfers_summary"]
                }

                Action.GET_QUOTES_PAID -> {
                    getQuotesPaidSummary(year, month)["quotes_paid_summary"]
                }

                else -> {
                    throw GetInfoException("Fail to obtain summary object")
                }
            }
            currentSummary.let {
                if (it != null) {
                    summaries.add(it)
                    totalCount += it["count"] as Int
                }
            }
        }

        for (summary in summaries) {
            val count = if (retrievedCount + summary["count"] as Int > large) large - retrievedCount else 0
            val retrievedActions = when (action) {
                Action.GET_CONNECTIONS -> {
                    getConnections(0, 0, summary, count, false)["connections"] as List<Map<String, String>>
                }

                Action.GET_RECHARGES -> {
                    getRecharges(0, 0, summary, count, false)["recharges"] as List<Map<String, String>>
                }

                Action.GET_TRANSFERS -> {
                    getTransfers(0, 0, summary, count, false)["transfers"] as List<Map<String, String>>
                }

                Action.GET_QUOTES_PAID -> {
                    getQuotesPaid(0, 0, summary, count, false)["quotes_paid"] as List<Map<String, String>>
                }

                else -> {
                    throw GetInfoException("Fail to obtain the ${actionKeys[action]}")
                }
            }
            retrievedActions.let {
                if (it.isNotEmpty()) {
                    actions.addAll(it)
                    retrievedCount = +it.size
                }
            }
        }

        return mapOf(
            "lasts" to actions
        )
    }

}