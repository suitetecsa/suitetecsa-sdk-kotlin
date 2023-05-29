package cu.suitetecsa.sdk.nauta.framekork

import cu.suitetecsa.sdk.nauta.core.*
import cu.suitetecsa.sdk.nauta.core.exceptions.LoginException
import cu.suitetecsa.sdk.nauta.core.exceptions.LogoutException
import cu.suitetecsa.sdk.nauta.core.exceptions.OperationException
import cu.suitetecsa.sdk.nauta.framework.model.DataSession
import cu.suitetecsa.sdk.nauta.framework.model.NautaConnectInformation
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.framework.network.ConnectPortalCommunicator
import cu.suitetecsa.sdk.nauta.framework.network.UserPortalCommunicator
import cu.suitetecsa.sdk.nauta.domain.model.*

class NautaApi(
    private val connectPortalCommunicator: ConnectPortalCommunicator,
    private val connectPortalScraper: ConnectPortalScraper,
    private val userPortalCommunicator: UserPortalCommunicator,
    private val userPortalScraper: UserPortalScraper
) {
    private var username = ""
    private var password = ""

    private var actionLogin = ""
    private var wlanUserIp = ""
    private var csrfHw = ""

    val isNautaHome: Boolean
        get() = userPortalCommunicator.isNautaHome

    val isConnected: Boolean
        get() {
            return when (val result = connectPortalCommunicator.checkConnection()) {
                is ResultType.Error -> false
                is ResultType.Success -> connectPortalScraper.parseCheckConnections(result.result ?: "")
            }
        }

    var dataSession: DataSession
        get() = connectPortalCommunicator.dataSession
        set(value) {
            connectPortalCommunicator.dataSession = value
        }

    val remainingTime: Long
        get() {
            when (val time = connectPortalCommunicator.remainingTime) {
                is ResultType.Error -> throw time.throwable
                is ResultType.Success -> return time.result?.toSeconds() ?: 0L
            }
        }

    val connectInformation: NautaConnectInformation
        get() {
            if (username.isBlank() || password.isBlank()) throw LoginException("username and password are required")
            if (csrfHw.isBlank()) init()
            when (val result = connectPortalCommunicator.getNautaConnectInformation(
                makeUrl(Action.LOAD_USER_INFORMATION, connectPortal), mapOf(
                    "username" to username,
                    "password" to password,
                    "wlanuserip" to wlanUserIp,
                    "CSRFHW" to csrfHw,
                    "lang" to ""
                )
            )) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> return connectPortalScraper.parseNautaConnectInformation(result.result ?: "")
            }
        }

    var credentials: Pair<String, String>
        get() = Pair(username, password)
        set(value) {
            val (user, pwd) = value
            username = user
            password = pwd
        }

    private fun init() {
        when (val landingResult = connectPortalCommunicator.checkConnection()) {
            is ResultType.Error -> throw landingResult.throwable
            is ResultType.Success -> {
                val (action, data) = connectPortalScraper.parseActionForm(landingResult.result ?: "")
                when (val loginResult = connectPortalCommunicator.getLoginPage(action, data)) {
                    is ResultType.Error -> throw loginResult.throwable
                    is ResultType.Success -> {
                        val (loginAction, loginData) = connectPortalScraper.parseActionForm(loginResult.result ?: "")
                        wlanUserIp = loginData["wlanuserip"] ?: ""
                        csrfHw = loginData["CSRFHW"] ?: ""
                        actionLogin = loginAction
                    }
                }
            }
        }
    }

    fun connect() {
        if (isConnected) throw LoginException("You are logged in")
        if (username.isBlank() || password.isBlank()) throw LoginException("username and password are required")
        init()
        when (val result = connectPortalCommunicator.connect(
            actionLogin, mapOf(
                "CSRFHW" to csrfHw,
                "wlanuserip" to wlanUserIp,
                "username" to username,
                "password" to password
            )
        )) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {
                val attributeUuid = connectPortalScraper.parseAttributeUUID(result.result ?: "")
                if (attributeUuid.isBlank()) throw OperationException("")
                connectPortalCommunicator.dataSession = DataSession(username, csrfHw, wlanUserIp, attributeUuid)
            }
        }
    }

    fun disconnect() {
        if (isConnected) {
            when (val result = connectPortalCommunicator.disconnect()) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    if (!connectPortalScraper.isSuccessLogout(
                            result.result ?: ""
                        )
                    ) throw LogoutException("Fail to logout ${result.result?.take(100)}")
                    connectPortalCommunicator.dataSession = DataSession("", "", "", "")
                }
            }
        }
    }

    val captchaImage: ByteArray
        get() = userPortalCommunicator.captchaImage
    val userInformation: NautaUser
        get() {
            when (val result = userPortalCommunicator.userInformation) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    return userPortalScraper.parseNautaUser(result.result ?: "")
                }
            }
        }

    private fun loadCsrf(action: Action) {
        when (val csrfResult = userPortalCommunicator.loadCsrf(action)) {
            is ResultType.Error -> throw csrfResult.throwable
            is ResultType.Success -> {
                userPortalCommunicator.csrf = userPortalScraper.parseCsrfToken(csrfResult.result ?: "")
            }
        }
    }

    fun login(captchaCode: String): NautaUser {
        if (userPortalCommunicator.csrf.isBlank()) loadCsrf(Action.LOGIN)
        when (val result = userPortalCommunicator.login(username, password, captchaCode)
        ) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {
                val user = userPortalScraper.parseNautaUser(result.result ?: "")
                userPortalCommunicator.isNautaHome = !user.offer.isNullOrEmpty()
                return user
            }
        }
    }

    fun topUp(rechargeCode: String) {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.RECHARGE)
        when (val result = userPortalCommunicator.topUp(rechargeCode)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {}
        }
    }

    fun transferFunds(amount: Float, destinationAccount: String?) {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.TRANSFER)
        when (val result = userPortalCommunicator.transferFunds(amount, password, destinationAccount)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {}
        }
    }

    fun changePassword(newPassword: String) {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.TRANSFER)
        when (val result = userPortalCommunicator.changePassword(password, newPassword)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {}
        }
    }

    fun changeEmailPassword(oldPassword: String, newPassword: String) {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.TRANSFER)
        when (val result = userPortalCommunicator.changeEmailPassword(oldPassword, newPassword)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> {}
        }
    }

    fun getConnectionsSummary(year: Int, month: Int): ConnectionsSummary {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.GET_CONNECTIONS)
        when (val result = userPortalCommunicator.getConnectionsSummary(year, month)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> return userPortalScraper.parseConnectionsSummary(result.result ?: "")
        }
    }

    fun getRechargesSummary(year: Int, month: Int): RechargesSummary {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.GET_RECHARGES)
        when (val result = userPortalCommunicator.getRechargesSummary(year, month)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> return userPortalScraper.parseRechargesSummary(result.result ?: "")
        }
    }

    fun getTransfersSummary(year: Int, month: Int): TransfersSummary {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.GET_TRANSFERS)
        when (val result = userPortalCommunicator.getTransfersSummary(year, month)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> return userPortalScraper.parseTransfersSummary(result.result ?: "")
        }
    }

    fun getQuotesPaidSummary(year: Int, month: Int): QuotesPaidSummary {
        if (userPortalCommunicator.csrf.isBlank()) throw LoginException("Session is not initialized")
        loadCsrf(Action.GET_QUOTES_PAID)
        when (val result = userPortalCommunicator.getQuotesPaidSummary(year, month)) {
            is ResultType.Error -> throw result.throwable
            is ResultType.Success -> return userPortalScraper.parseQuotesPaidSummary(result.result ?: "")
        }
    }

    fun getConnections(
        connectionsSummary: ConnectionsSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): List<Connection> {
        val connections = mutableListOf<Connection>()
        val internalLarge = if (large == 0) connectionsSummary.count else large
        if (connectionsSummary.count != 0) {
            when (val result = userPortalCommunicator.getConnections(connectionsSummary, internalLarge, reversed)) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    for (page in result.result) {
                        val connects = userPortalScraper.parseConnections(page ?: "")
                        connections.addAll(if (reversed) connects.reversed() else connects)
                    }
                }
            }
        }
        return connections.take(internalLarge)
    }

    fun getRecharges(rechargesSummary: RechargesSummary, large: Int = 0, reversed: Boolean = false): List<Recharge> {
        val recharges = mutableListOf<Recharge>()
        val internalLarge = if (large == 0) rechargesSummary.count else large
        if (rechargesSummary.count != 0) {
            when (val result = userPortalCommunicator.getRecharges(rechargesSummary, internalLarge, reversed)) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    for (page in result.result) {
                        val rechargeList = userPortalScraper.parseRecharges(page ?: "")
                        recharges.addAll(if (reversed) rechargeList.reversed() else rechargeList)
                    }
                }
            }
        }
        return recharges.take(internalLarge)
    }

    fun getTransfers(transfersSummary: TransfersSummary, large: Int = 0, reversed: Boolean = false): List<Transfer> {
        val transfers = mutableListOf<Transfer>()
        val internalLarge = if (large == 0) transfersSummary.count else large
        if (transfersSummary.count != 0) {
            when (val result = userPortalCommunicator.getTransfers(transfersSummary, internalLarge, reversed)) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    for (page in result.result) {
                        val transferList = userPortalScraper.parseTransfers(page ?: "")
                        transfers.addAll(if (reversed) transferList.reversed() else transferList)
                    }
                }
            }
        }
        return transfers.take(internalLarge)
    }

    fun getQuotesPaid(
        quotesPaidSummary: QuotesPaidSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): List<QuotePaid> {
        val quotesPaid = mutableListOf<QuotePaid>()
        val internalLarge = if (large == 0) quotesPaidSummary.count else large
        if (quotesPaidSummary.count != 0) {
            when (val result = userPortalCommunicator.getQuotesPaid(quotesPaidSummary, internalLarge, reversed)) {
                is ResultType.Error -> throw result.throwable
                is ResultType.Success -> {
                    for (page in result.result) {
                        val quotePaidList = userPortalScraper.parseQuotesPaid(page ?: "")
                        quotesPaid.addAll(if (reversed) quotePaidList.reversed() else quotePaidList)
                    }
                }
            }
        }
        return quotesPaid.take(internalLarge)
    }
}