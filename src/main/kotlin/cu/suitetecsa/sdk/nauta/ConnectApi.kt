package cu.suitetecsa.sdk.nauta

import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import cu.suitetecsa.sdk.nauta.core.exceptions.*
import cu.suitetecsa.sdk.nauta.core.extensions.toSeconds
import cu.suitetecsa.sdk.nauta.domain.model.*
import cu.suitetecsa.sdk.nauta.domain.model.Recharge
import cu.suitetecsa.sdk.nauta.domain.model.ResultType.Failure
import cu.suitetecsa.sdk.nauta.domain.model.ResultType.Success
import cu.suitetecsa.sdk.nauta.domain.model.Transfer
import cu.suitetecsa.sdk.nauta.scraper.ConnectPortalScraper
import cu.suitetecsa.sdk.nauta.scraper.ConnectPortalScraperImpl
import cu.suitetecsa.sdk.nauta.scraper.UserPortalScraper
import cu.suitetecsa.sdk.nauta.scraper.UserPortalScrapperImpl
import cu.suitetecsa.sdk.nauta.util.*
import cu.suitetecsa.sdk.network.Action
import cu.suitetecsa.sdk.network.HttpResponse
import cu.suitetecsa.sdk.network.JsoupPortalCommunicator
import cu.suitetecsa.sdk.network.PortalCommunicator
import cu.suitetecsa.sdk.util.ExceptionHandler
import kotlin.math.ceil
import cu.suitetecsa.sdk.nauta.util.Recharge as ActionRecharge
import cu.suitetecsa.sdk.nauta.util.Transfer as ActionTransfer

class ConnectApi(
    private val portalCommunicator: PortalCommunicator,
    private val connectPortalScraper: ConnectPortalScraper,
    private val userPortalCommunicator: PortalCommunicator,
    private val userPortalScraper: UserPortalScraper
) {
    private var username = ""
    private var password = ""

    private var actionLogin = ""
    private var wlanUserIp = ""
    private var csrfHw = ""
    private var csrf = ""
    private var attributeUUID = ""

    private val notLoggedInExceptionHandler = ExceptionHandler
        .builder(NotLoggedInException::class.java)
        .build()

    var isNautaHome: Boolean = false
        private set

    val isConnected: ResultType<Boolean>
        get() {
            return portalCommunicator.performAction(CheckConnection()) {
                connectPortalScraper.parseCheckConnections(it.text ?: "")
            }
        }

    var dataSession: DataSession = DataSession("", "", "", "")

    val remainingTime: ResultType<Long>
        get() {
            return portalCommunicator.performAction(
                LoadUserInformation(
                    username = username,
                    wlanUserIp = wlanUserIp,
                    csrfHw = csrfHw,
                    attributeUUID = attributeUUID,
                    portal = Connect
                )
            ) { it.text?.toSeconds() ?: 0L }
        }

    val connectInformation: ResultType<NautaConnectInformation>
        get() {
            if (username.isBlank() || password.isBlank()) throw LoginException("username and password are required")
            if (csrfHw.isBlank()) init()
            return portalCommunicator.performAction(
                LoadUserInformation(
                    username = username,
                    password = password,
                    wlanUserIp = wlanUserIp,
                    csrfHw = csrfHw,
                    portal = Connect
                )
            ) {
                connectPortalScraper.parseNautaConnectInformation(it.text ?: "")
            }
        }

    private fun init() {
        when (val landingResult = portalCommunicator.performAction(CheckConnection()) {
            connectPortalScraper.parseActionForm(it.text ?: "")
        }) {
            is Failure -> throw landingResult.throwable
            is Success -> {
                val (action, data) = landingResult.result
                when (val loginResult = portalCommunicator.performAction(GetPage(action, data)) {
                    connectPortalScraper.parseActionForm(it.text ?: "")
                }) {
                    is Failure -> throw loginResult.throwable
                    is Success -> {
                        val (loginAction, loginData) = loginResult.result
                        wlanUserIp = data["wlanuserip"] ?: ""
                        csrfHw = loginData["CSRFHW"] ?: ""
                        actionLogin = loginAction
                    }
                }
            }
        }
    }

    fun setCredentials(username: String, password: String) {
        this.username = username
        this.password = password
    }

    fun connect(): ResultType<String> {
        val loginExceptionHandler = ExceptionHandler.builder(LoginException::class.java).build()

        when (val it = isConnected) {
            is Failure -> return Failure(
                loginExceptionHandler.handleException(
                    "Failed to connect",
                    listOf(it.throwable.message ?: "")
                )
            )

            is Success -> if (it.result) return Failure(
                loginExceptionHandler.handleException(
                    "Fail to connect",
                    listOf("Already connected")
                )
            )
        }

        if (username.isBlank() || password.isBlank())
            return Failure(
                loginExceptionHandler.handleException(
                    "Fail to connect",
                    listOf("username and password are required")
                )
            )

        init()

        portalCommunicator.performAction(
            Login(
                csrf = csrfHw,
                wlanUserIp = wlanUserIp,
                username = username,
                password = password,
                portal = Connect
            )
        ) {
            try {
                connectPortalScraper.parseAttributeUUID(it.text ?: "")
            } catch (exc: LoadInfoException) {
                ""
            }
        }.also {
            return when (it) {
                is Failure -> return it
                is Success -> {
                    if (it.result.isBlank()) Failure(
                        loginExceptionHandler.handleException(
                            "Fail to connect",
                            listOf("Unknown error")
                        )
                    )
                    else {
                        dataSession = DataSession(username, csrfHw, wlanUserIp, it.result)
                        Success("Connected")
                    }
                }
            }
        }
    }

    fun disconnect(): ResultType<String> {
        val logoutExceptionHandler = ExceptionHandler.builder(LogoutException::class.java).build()
        isConnected.also {
            when (it) {
                is Failure -> return Failure(
                    logoutExceptionHandler.handleException(
                        "Failed to disconnect",
                        listOf(it.throwable.message ?: "Unknown exception")
                    )
                )

                is Success -> {
                    portalCommunicator.performAction(
                        Logout(
                            username,
                            wlanUserIp,
                            csrfHw,
                            attributeUUID
                        )
                    ) { response ->
                        connectPortalScraper.isSuccessLogout(response.text ?: "")
                    }.also { logoutResult ->
                        return when (logoutResult) {
                            is Failure -> Failure(
                                logoutExceptionHandler.handleException(
                                    "Failed to disconnect",
                                    listOf(logoutResult.throwable.message ?: "Unknown exception")
                                )
                            )

                            is Success -> if (!logoutResult.result) Failure(
                                logoutExceptionHandler.handleException(
                                    "Failed to disconnect",
                                    listOf("")
                                )
                            )
                            else {
                                dataSession = DataSession("", "", "", "")
                                Success("Disconnected")
                            }
                        }
                    }
                }
            }
        }
    }

    val captchaImage: ResultType<ByteArray>
        get() = userPortalCommunicator.performAction(GetCaptcha) { it.content ?: ByteArray(0) }
    val userInformation: ResultType<NautaUser>
        get() {
            if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
                "Failed to get user information",
                listOf("You are not logged in")
            )
            return userPortalCommunicator.performAction(LoadUserInformation(portal = User)) {
                userPortalScraper.parseNautaUser(it.text ?: "")
            }
        }

    private fun loadCsrf(action: Action) {
        when (val csrfResult =
            userPortalCommunicator.performAction("https://www.portal.nauta.cu${action.csrfUrl() ?: action.url()}") {
                userPortalScraper.parseCsrfToken(it.text ?: "")
            }) {
            is Failure -> throw csrfResult.throwable
            is Success -> {
                csrf = userPortalScraper.parseCsrfToken(csrfResult.result)
            }
        }
    }

    fun login(captchaCode: String): ResultType<NautaUser> {
        val preAction = Login(
            username = username,
            password = password,
            captchaCode = captchaCode,
            portal = User
        )
        if (csrf.isBlank()) loadCsrf(preAction)
        val action =
            Login(
                csrf = csrf,
                username = username,
                password = password,
                captchaCode = captchaCode,
                portal = User
            )
        return userPortalCommunicator.performAction(action) {
            val user =
                userPortalScraper.parseNautaUser(
                    it.text ?: "",
                    ExceptionHandler.builder(LoginException::class.java).build()
                )
            isNautaHome = !user.offer.isNullOrEmpty()
            user
        }
    }

    fun topUp(rechargeCode: String): ResultType<NautaUser> {
        val preAction = ActionRecharge(rechargeCode = rechargeCode)
        if (csrf.isBlank()) return Failure(
            notLoggedInExceptionHandler.handleException(
                "Failed to top up",
                listOf("You are not logged in")
            )
        )
        loadCsrf(preAction)
        val action = ActionRecharge(csrf = csrf, rechargeCode = rechargeCode)
        return when (val result = userPortalCommunicator.performAction(action) {
            userPortalScraper.parseErrors(
                it.text ?: "",
                "Failed to top up",
                ExceptionHandler.builder(RechargeException::class.java).build()
            )
        }) {
            is Failure -> throw result.throwable
            is Success -> userInformation
        }
    }

    fun transferFunds(amount: Float, destinationAccount: String?): ResultType<NautaUser> {
        val preAction = ActionTransfer(amount = amount, destinationAccount = destinationAccount, password = password)
        if (csrf.isBlank()) return Failure(
            notLoggedInExceptionHandler.handleException(
                "Failed to transfer funds",
                listOf("You are not logged in")
            )
        )
        loadCsrf(preAction)
        val action =
            ActionTransfer(csrf = csrf, amount = amount, destinationAccount = destinationAccount, password = password)
        return when (val result = userPortalCommunicator.performAction(action) {
            userPortalScraper.parseErrors(it.text ?: "")
        }) {
            is Failure -> throw result.throwable
            is Success -> userInformation
        }
    }

    fun changePassword(newPassword: String): ResultType<String> {
        val preAction = ChangePassword(
            oldPassword = password,
            newPassword = newPassword
        )
        if (csrf.isBlank()) return Failure(
            notLoggedInExceptionHandler.handleException(
                "Failed to change password",
                listOf("You are not logged in")
            )
        )
        loadCsrf(preAction)
        val action = ChangePassword(
            csrf = csrf,
            oldPassword = password,
            newPassword = newPassword
        )
        return when (val result = userPortalCommunicator.performAction(action) {
            userPortalScraper.parseErrors(it.text ?: "")
        }) {
            is Failure -> throw result.throwable
            is Success -> Success(newPassword)
        }
    }

    fun changeEmailPassword(oldPassword: String, newPassword: String): ResultType<String> {
        val preAction = ChangePassword(
            oldPassword = oldPassword,
            newPassword = newPassword,
            changeMail = true
        )
        if (csrf.isBlank()) return Failure(
            notLoggedInExceptionHandler.handleException(
                "Failed to change email password",
                listOf("You are not logged in")
            )
        )
        loadCsrf(preAction)
        val action =
            ChangePassword(
                csrf = csrf,
                oldPassword = oldPassword,
                newPassword = newPassword,
                changeMail = true
            )
        return when (val result = userPortalCommunicator.performAction(action) {
            userPortalScraper.parseErrors(it.text ?: "")
        }) {
            is Failure -> throw result.throwable
            is Success -> Success(newPassword)
        }
    }

    fun getConnectionsSummary(year: Int, month: Int): ResultType<ConnectionsSummary> {
        val preAction = GetSummary(
            year = year,
            month = month,
            type = ActionType.Connections
        )
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get connections summary",
            listOf("You are not logged in")
        )
        loadCsrf(preAction)
        val action = GetSummary(
            csrf = csrf,
            year = year,
            month = month,
            type = ActionType.Connections
        )
        return userPortalCommunicator.performAction(action) {
            userPortalScraper.parseConnectionsSummary(it.text ?: "")
        }
    }

    fun getRechargesSummary(year: Int, month: Int): ResultType<RechargesSummary> {
        val preAction = GetSummary(
            year = year,
            month = month,
            type = ActionType.Recharges
        )
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get connections summary",
            listOf("You are not logged in")
        )
        loadCsrf(preAction)
        val action = GetSummary(
            csrf = csrf,
            year = year,
            month = month,
            type = ActionType.Recharges
        )
        return userPortalCommunicator.performAction(action) {
            userPortalScraper.parseRechargesSummary(it.text ?: "")
        }
    }

    fun getTransfersSummary(year: Int, month: Int): ResultType<TransfersSummary> {
        val preAction = GetSummary(
            year = year,
            month = month,
            type = ActionType.Transfers
        )
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get transfers summary",
            listOf("You are not logged in")
        )
        loadCsrf(preAction)
        val action = GetSummary(
            csrf = csrf,
            year = year,
            month = month,
            type = ActionType.Transfers
        )
        return userPortalCommunicator.performAction(action) {
            userPortalScraper.parseTransfersSummary(it.text ?: "")
        }
    }

    fun getQuotesPaidSummary(year: Int, month: Int): ResultType<QuotesPaidSummary> {
        val preAction = GetSummary(
            year = year,
            month = month,
            type = ActionType.QuotesPaid
        )
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get quotes paid summary",
            listOf("You are not logged in")
        )
        loadCsrf(preAction)
        val action = GetSummary(
            csrf = csrf,
            year = year,
            month = month,
            type = ActionType.QuotesPaid
        )
        return userPortalCommunicator.performAction(action) {
            userPortalScraper.parseQuotesPaidSummary(it.text ?: "")
        }
    }

    @JvmOverloads
    fun getConnections(
        connectionsSummary: ConnectionsSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): ResultType<List<Connection>> {
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get connections",
            listOf("You are not logged in")
        )
        val internalLarge = if (large == 0) connectionsSummary.count else large
        if (connectionsSummary.count != 0) {
            val action = GetActions(
                connectionsSummary.count,
                connectionsSummary.yearMonthSelected,
                internalLarge,
                reversed,
                ActionType.Connections
            )
            return getActions(action) {
                val connects = userPortalScraper.parseConnections(it.text ?: "")
                if (reversed) connects.reversed() else connects
            }
        }
        return Success(emptyList())
    }

    @JvmOverloads
    fun getRecharges(
        rechargesSummary: RechargesSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): ResultType<List<Recharge>> {
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get recharges",
            listOf("You are not logged in")
        )
        val internalLarge = if (large == 0) rechargesSummary.count else large
        if (rechargesSummary.count != 0) {
            val action = GetActions(
                rechargesSummary.count,
                rechargesSummary.yearMonthSelected,
                internalLarge,
                reversed,
                ActionType.Connections
            )
            return getActions(action) {
                val recharges = userPortalScraper.parseRecharges(it.text ?: "")
                if (reversed) recharges.reversed() else recharges
            }
        }
        return Success(emptyList())
    }

    @JvmOverloads
    fun getTransfers(
        transfersSummary: TransfersSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): ResultType<List<Transfer>> {
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get transfers",
            listOf("You are not logged in")
        )
        val internalLarge = if (large == 0) transfersSummary.count else large
        if (transfersSummary.count != 0) {
            val action = GetActions(
                transfersSummary.count,
                transfersSummary.yearMonthSelected,
                internalLarge,
                reversed,
                ActionType.Connections
            )
            return getActions(action) {
                val transfers = userPortalScraper.parseTransfers(it.text ?: "")
                if (reversed) transfers.reversed() else transfers
            }
        }
        return Success(emptyList())
    }

    @JvmOverloads
    fun getQuotesPaid(
        quotesPaidSummary: QuotesPaidSummary,
        large: Int = 0,
        reversed: Boolean = false
    ): ResultType<List<QuotePaid>> {
        if (csrf.isBlank()) throw notLoggedInExceptionHandler.handleException(
            "Failed to get quotes paid",
            listOf("You are not logged in")
        )
        val internalLarge = if (large == 0) quotesPaidSummary.count else large
        if (quotesPaidSummary.count != 0) {
            val action = GetActions(
                quotesPaidSummary.count,
                quotesPaidSummary.yearMonthSelected,
                internalLarge,
                reversed,
                ActionType.Connections
            )
            return getActions(action) {
                val quotesPaid = userPortalScraper.parseQuotesPaid(it.text ?: "")
                if (reversed) quotesPaid.reversed() else quotesPaid
            }
        }
        return Success(emptyList())
    }

    private fun <T> getActions(
        action: Action,
        transform: (HttpResponse) -> List<T>
    ): ResultType<List<T>> {
        val large = action.large()
        val count = action.count()
        val reversed = action.reversed()
        val yearMonthSelected = action.yearMonthSelected()
        val url = action.url()

        val actionList = mutableListOf<T>()
        val internalLarge = if (large == 0 || large > count) count else large
        if (count != 0) {
            val totalPages = ceil(count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while ((actionList.size - rest) < internalLarge && (currentPage in 1..totalPages)) {
                val page = if (currentPage != 1) currentPage else null
                val currentUrl = "$url$yearMonthSelected/$count${page?.let { "/$it" } ?: ""}"
                when (val result = portalCommunicator.performAction(currentUrl, transform = transform)) {
                    is Failure -> return Failure(result.throwable)
                    is Success -> {
                        actionList.addAll(result.result)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return Success(actionList.take(internalLarge))
    }

    class Builder {
        private var portalCommunicator: PortalCommunicator? = null
        private var connectPortalScraper: ConnectPortalScraper? = null
        private var userPortalCommunicator: PortalCommunicator? = null
        private var userPortalScraper: UserPortalScraper? = null

        fun connectPortalCommunicator(communicator: PortalCommunicator): Builder {
            portalCommunicator = communicator
            return this
        }

        fun connectPortalScraper(scraper: ConnectPortalScraper): Builder {
            connectPortalScraper = scraper
            return this
        }

        fun userPortalCommunicator(communicator: PortalCommunicator): Builder {
            userPortalCommunicator = communicator
            return this
        }

        fun userPortalScraper(scraper: UserPortalScraper): Builder {
            userPortalScraper = scraper
            return this
        }

        fun build(): ConnectApi {
            return ConnectApi(
                portalCommunicator = portalCommunicator ?: JsoupPortalCommunicator.Builder().build(),
                connectPortalScraper = connectPortalScraper ?: ConnectPortalScraperImpl.builder().build(),
                userPortalCommunicator = userPortalCommunicator ?: JsoupPortalCommunicator.Builder().build(),
                userPortalScraper = userPortalScraper ?: UserPortalScrapperImpl()
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}