package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.core.Portal
import cu.suitetecsa.sdk.nauta.core.exceptions.TransferFundsException
import cu.suitetecsa.sdk.nauta.core.makeUrl
import cu.suitetecsa.sdk.nauta.core.userPortal
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.domain.model.*
import kotlin.math.ceil

class JsoupUserPortalCommunicator(
    private val nautaSession: NautaSession
) : UserPortalCommunicator {
    private var _csrf = ""
    private var _isNautaHome = false

    override var csrf: String
        get() = _csrf
        set(value) {
            _csrf = value
        }

    override var isNautaHome: Boolean
        get() = _isNautaHome
        set(value) {
            _isNautaHome = value
        }

    init {
        nautaSession.setPortalManager(Portal.USER)
    }

    override fun loadCsrf(action: Action): ResultType<String?> {
        val url = when (action) {
            Action.GET_CONNECTIONS -> makeUrl(action, userPortal, true, "base")
            Action.GET_RECHARGES -> makeUrl(action, userPortal, true, "base")
            Action.GET_TRANSFERS -> makeUrl(action, userPortal, true, "base")
            Action.GET_QUOTES_PAID -> makeUrl(action, userPortal, true, "base")
            else -> makeUrl(action, userPortal)
        }
        return when (val result = nautaSession.get(url)) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override val captchaImage: ByteArray
        get() {
            when (val loginResponse = nautaSession.get(makeUrl(Action.LOGIN, userPortal))) {
                is ResultType.Error -> throw loginResponse.throwable
                is ResultType.Success -> {
                    loginResponse.result.cookies?.forEach { (key, value) ->
                        nautaSession.cookies[key] = value
                    }
                    when (val response = nautaSession.get(
                        url = "https://www.portal.nauta.cu/captcha/?",
                        ignoreContentType = true,
                        timeout = 25000
                    )) {
                        is ResultType.Error -> throw response.throwable
                        is ResultType.Success -> {
                            return response.result.content ?: ByteArray(0)
                        }
                    }
                }
            }
        }
    override val userInformation: ResultType<String?>
        get() {
            return when (val result = nautaSession.get(makeUrl(Action.LOAD_USER_INFORMATION, userPortal))) {
                is ResultType.Error -> ResultType.Error(result.throwable)
                is ResultType.Success -> ResultType.Success(result.result.text)
            }
        }

    override fun login(username: String, password: String, captchaCode: String): ResultType<String?> {
        return when (val result = nautaSession.post(
            makeUrl(Action.LOGIN, userPortal), mapOf(
                "csrf" to _csrf,
                "login_user" to username,
                "password_user" to password,
                "captcha" to captchaCode,
                "btn_submit" to ""
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun topUp(rechargeCode: String): ResultType<String?> {
        return when (val result = nautaSession.post(
            makeUrl(Action.RECHARGE, userPortal),
            mapOf("csrf" to _csrf, "recharge_code" to rechargeCode, "btn_submit" to "")
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun transferFunds(amount: Float, password: String, destinationAccount: String?): ResultType<String?> {
        val data = mutableMapOf(
            "csrf" to _csrf,
            "transfer" to String.format("%.2f", amount).replace(".", ","),
            "password_user" to password,
            "action" to "checkdata"
        )
        if (destinationAccount.isNullOrEmpty() && !_isNautaHome) {
            throw TransferFundsException("The destination account  is required. Your account are not associated to Nauta Home")
        }
        if (!destinationAccount.isNullOrEmpty()) data["id_cuenta"] = destinationAccount
        return when (val result = nautaSession.post(makeUrl(Action.TRANSFER, userPortal), data)) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun changePassword(oldPassword: String, newPassword: String): ResultType<String?> {
        return when (val result = nautaSession.post(
            makeUrl(Action.CHANGE_PASSWORD, userPortal), mapOf(
                "csrf" to _csrf,
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun changeEmailPassword(oldPassword: String, newPassword: String): ResultType<String?> {
        return when (val result = nautaSession.post(
            makeUrl(Action.CHANGE_EMAIL_PASSWORD, userPortal), mapOf(
                "csrf" to _csrf,
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun getConnectionsSummary(year: Int, month: Int): ResultType<String?> {
        val yearMonth = "$year-${String.format("%02d", month)}"
        return when (val result = nautaSession.post(
            makeUrl(Action.GET_CONNECTIONS, userPortal, true, "summary"), mapOf(
                "csrf" to _csrf, "year_month" to yearMonth, "list_type" to "service_detail"
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun getRechargesSummary(year: Int, month: Int): ResultType<String?> {
        val yearMonth = "$year-${String.format("%02d", month)}"
        return when (val result = nautaSession.post(
            makeUrl(Action.GET_RECHARGES, userPortal, true, "summary"), mapOf(
                "csrf" to _csrf, "year_month" to yearMonth, "list_type" to "service_detail"
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun getTransfersSummary(year: Int, month: Int): ResultType<String?> {
        val yearMonth = "$year-${String.format("%02d", month)}"
        return when (val result = nautaSession.post(
            makeUrl(Action.GET_TRANSFERS, userPortal, true, "summary"), mapOf(
                "csrf" to _csrf, "year_month" to yearMonth, "list_type" to "service_detail"
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun getQuotesPaidSummary(year: Int, month: Int): ResultType<String?> {
        val yearMonth = "$year-${String.format("%02d", month)}"
        return when (val result = nautaSession.post(
            makeUrl(Action.GET_QUOTES_PAID, userPortal, true, "summary"), mapOf(
                "csrf" to _csrf, "year_month" to yearMonth, "list_type" to "service_detail"
            )
        )) {
            is ResultType.Error -> ResultType.Error(result.throwable)
            is ResultType.Success -> ResultType.Success(result.result.text)
        }
    }

    override fun getConnections(
        connectionsSummary: ConnectionsSummary,
        large: Int,
        reversed: Boolean
    ): ResultType<List<String?>> {
        val connections = mutableListOf<String?>()
        val internalLarge = if (large == 0 || large > connectionsSummary.count) connectionsSummary.count else large
        if (connectionsSummary.count != 0) {
            val totalPages = ceil(connectionsSummary.count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while (((connections.size * 14) - rest) < internalLarge && currentPage >= 1) {
                val page = if (currentPage != 1) currentPage else null
                val url = makeUrl(
                    Action.GET_CONNECTIONS,
                    userPortal,
                    true,
                    "list",
                    connectionsSummary.yearMonthSelected,
                    connectionsSummary.count,
                    page
                )
                when (val result = nautaSession.get(url)) {
                    is ResultType.Error -> return ResultType.Error(result.throwable)
                    is ResultType.Success -> {
                        connections.add(result.result.text)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return ResultType.Success(connections)
    }

    override fun getRecharges(
        rechargesSummary: RechargesSummary,
        large: Int,
        reversed: Boolean
    ): ResultType<List<String?>> {
        val recharges = mutableListOf<String?>()
        val internalLarge = if (large == 0 || large > rechargesSummary.count) rechargesSummary.count else large
        if (rechargesSummary.count != 0) {
            val totalPages = ceil(rechargesSummary.count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while (((recharges.size * 14) - rest) < internalLarge && currentPage >= 1) {
                val page = if (currentPage != 1) currentPage else null
                val url = makeUrl(
                    Action.GET_RECHARGES,
                    userPortal,
                    true,
                    "list",
                    rechargesSummary.yearMonthSelected,
                    rechargesSummary.count,
                    page
                )
                when (val result = nautaSession.get(url)) {
                    is ResultType.Error -> return ResultType.Error(result.throwable)
                    is ResultType.Success -> {
                        recharges.add(result.result.text)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return ResultType.Success(recharges)
    }

    override fun getTransfers(
        transfersSummary: TransfersSummary,
        large: Int,
        reversed: Boolean
    ): ResultType<List<String?>> {
        val transfers = mutableListOf<String?>()
        val internalLarge = if (large == 0 || large > transfersSummary.count) transfersSummary.count else large
        if (transfersSummary.count != 0) {
            val totalPages = ceil(transfersSummary.count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while (((transfers.size * 14) - rest) < internalLarge && currentPage >= 1) {
                val page = if (currentPage != 1) currentPage else null
                val url = makeUrl(
                    Action.GET_TRANSFERS,
                    userPortal,
                    true,
                    "list",
                    transfersSummary.yearMonthSelected,
                    transfersSummary.count,
                    page
                )
                when (val result = nautaSession.get(url)) {
                    is ResultType.Error -> return ResultType.Error(result.throwable)
                    is ResultType.Success -> {
                        transfers.add(result.result.text)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return ResultType.Success(transfers)
    }

    override fun getQuotesPaid(
        quotesPaidSummary: QuotesPaidSummary,
        large: Int,
        reversed: Boolean
    ): ResultType<List<String?>> {
        val quotesPaid = mutableListOf<String?>()
        val internalLarge = if (large == 0 || large > quotesPaidSummary.count) quotesPaidSummary.count else large
        if (quotesPaidSummary.count != 0) {
            val totalPages = ceil(quotesPaidSummary.count.toDouble() / 14.0).toInt()
            var currentPage = if (reversed) totalPages else 1
            val rest = if (reversed || currentPage == totalPages) totalPages % 14 else 0
            while (((quotesPaid.size * 14) - rest) < internalLarge && currentPage >= 1) {
                val page = if (currentPage != 1) currentPage else null
                val url = makeUrl(
                    Action.GET_QUOTES_PAID,
                    userPortal,
                    true,
                    "list",
                    quotesPaidSummary.yearMonthSelected,
                    quotesPaidSummary.count,
                    page
                )
                when (val result = nautaSession.get(url)) {
                    is ResultType.Error -> return ResultType.Error(result.throwable)
                    is ResultType.Success -> {
                        quotesPaid.add(result.result.text)
                    }
                }
                currentPage += if (reversed) -1 else 1
            }
        }
        return ResultType.Success(quotesPaid)
    }
}