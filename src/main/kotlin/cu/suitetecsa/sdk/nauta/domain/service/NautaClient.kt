package cu.suitetecsa.sdk.nauta.domain.service

import cu.suitetecsa.sdk.nauta.core.exceptions.LoadInfoException
import cu.suitetecsa.sdk.nauta.core.exceptions.LoginException
import cu.suitetecsa.sdk.nauta.core.exceptions.NotLoggedIn
import cu.suitetecsa.sdk.nauta.data.repository.NautaScrapper
import cu.suitetecsa.sdk.nauta.domain.model.*
import java.time.LocalDate

class NautaClient(private val provider: NautaScrapper) {
    private var userName: String? = null
    private var password: String? = null
    private var isLoggedInUser: Boolean = false

    val isNautaHome: Boolean
        get() = provider.isNautaHome

    val captchaImage: ByteArray
        get() = provider.getCaptcha()

    val remainingTime: String
        get() = provider.getRemainingTime()

    val connectInformation: Map<String, Any>
        get() {
            if (userName.isNullOrEmpty() || password.isNullOrEmpty()) throw LoginException("username and password are required")
            return provider.getInformationConnect(userName!!, password!!)
        }

    val userInformation
        get() = if (isLoggedInUser) provider.getInformationUser() else throw NotLoggedIn("You are not logged in")

    val isLoggedIn
        get() = provider.isLoggedIn

    val isConnected
        get() = provider.isConnected

    var dataSession: Map<String, String>
        get() = provider.getDataSession()
        set(value) {
            provider.loadDataSession(value)
        }

    fun getCredentials(): Pair<String, String> {
        return Pair(userName ?: "", password ?: "")
    }

    fun setCredentials(userName: String, password: String) {
        if (!userName.endsWith("@nauta.com.cu") || userName.endsWith("@nauta.co.cu")) throw LoadInfoException("Enter a valid username")
        if (password.length < 8) throw LoadInfoException("Enter a valid password")
        this.userName = userName
        this.password = password
    }

    fun connect() {
        if (userName.isNullOrEmpty() || password.isNullOrEmpty()) throw LoginException("username and password are required")
        provider.connect(userName!!, password!!)
    }

    fun disconnect() = provider.disconnect()

    fun login(captchaCode: String): NautaUser {
        if (userName.isNullOrEmpty() || password.isNullOrEmpty()) throw LoginException("username and password are required")
        if (captchaCode.isEmpty()) throw LoginException("captcha code is required")
        val userInfo = provider.login(userName!!, password!!, captchaCode)
        isLoggedInUser = true
        return userInfo
    }

    fun toUpBalance(rechargeCode: String) =
        if (isLoggedInUser) provider.toUpBalance(rechargeCode) else throw NotLoggedIn("You are not logged in")

    fun transferBalance(amount: Float, destinationAccount: String) {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (!destinationAccount.endsWith("@nauta.com.cu") || destinationAccount.endsWith("@nauta.co.cu")) throw IllegalArgumentException(
            "The destination account must end in @nauta.com.cu or @nauta.co.cu"
        )
        provider.transferFunds(amount, password!!, destinationAccount)
    }

    fun payNautaHome(amount: Float) =
        if (isLoggedInUser) provider.transferFunds(amount, password!!) else throw NotLoggedIn("You are not logged in")

    fun changePassword(newPassword: String) {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (newPassword.isEmpty()) throw IllegalArgumentException("the new password cannot be an empty string")
        if (newPassword.length < 8) throw IllegalArgumentException("the new password cannot be less than 8 characters")
        provider.changePassword(password!!, newPassword)
    }

    fun changeEmailPassword(oldPassword: String, newPassword: String) {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (oldPassword.isEmpty()) throw IllegalArgumentException("the old password cannot be an empty string")
        if (newPassword.isEmpty()) throw IllegalArgumentException("the new password cannot be an empty string")
        if (newPassword.length < 8) throw IllegalArgumentException("the new password cannot be less than 8 characters")
        provider.changeEmailPassword(oldPassword, newPassword)
    }

    fun getConnectionsSummary(year: Int, month: Int): ConnectionsSummary {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (year <= 2021) throw IllegalArgumentException("")
        if (month !in 1..12) throw IllegalArgumentException("")
        val now = LocalDate.now()
        if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
        return provider.getConnectionsSummary(year, month)
    }

    fun getConnections(
        year: Int, month: Int, connectionsSummary: ConnectionsSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Connection> {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        return if ((year <= 0 || month <= 0) && (connectionsSummary != null && connectionsSummary.count >= 0)) {
            provider.getConnections(0, 0, connectionsSummary, large, reversed)
        } else {
            if (year <= 2021) throw IllegalArgumentException("")
            if (month !in 1..12) throw IllegalArgumentException("")
            val now = LocalDate.now()
            if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
            provider.getConnections(year, month, connectionsSummary, large, reversed)
        }
    }

    fun getRechargesSummary(year: Int, month: Int): RechargesSummary {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (year <= 2021) throw IllegalArgumentException("")
        if (month !in 1..12) throw IllegalArgumentException("")
        val now = LocalDate.now()
        if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
        return provider.getRechargesSummary(year, month)
    }

    fun getRecharges(
        year: Int, month: Int, rechargesSummary: RechargesSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Recharge> {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        return if ((year == 0 || month == 0) && (rechargesSummary != null && rechargesSummary.count >= 0)) {
            provider.getRecharges(0, 0, rechargesSummary, large, reversed)
        } else {
            if (year <= 2021) throw IllegalArgumentException("")
            if (month !in 1..12) throw IllegalArgumentException("")
            val now = LocalDate.now()
            if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
            provider.getRecharges(year = year, month = month, large = large, reversed = reversed)
        }
    }

    fun getTransfersSummary(year: Int, month: Int): TransfersSummary {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (year <= 2021) throw IllegalArgumentException("")
        if (month !in 1..12) throw IllegalArgumentException("")
        val now = LocalDate.now()
        if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
        return provider.getTransfersSummary(year, month)
    }

    fun getTransfers(
        year: Int, month: Int, connectionsSummary: TransfersSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Transfer> {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        return if ((year == 0 || month == 0) && (connectionsSummary != null && connectionsSummary.count >= 0)) {
            provider.getTransfers(0, 0, connectionsSummary, large, reversed)
        } else {
            if (year <= 2021) throw IllegalArgumentException("")
            if (month !in 1..12) throw IllegalArgumentException("")
            val now = LocalDate.now()
            if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
            provider.getTransfers(year = year, month = month, large = large, reversed = reversed)
        }
    }

    fun getQuotesPaidSummary(year: Int, month: Int): QuotesPaidSummary {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        if (year <= 2021) throw IllegalArgumentException("")
        if (month !in 1..12) throw IllegalArgumentException("")
        val now = LocalDate.now()
        if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
        return provider.getQuotesPaidSummary(year, month)
    }

    fun getQuotesPaid(
        year: Int, month: Int, quotesPaidSummary: QuotesPaidSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<QuotePaid> {
        if (!isLoggedInUser) throw NotLoggedIn("You are not logged in")
        return if ((year == 0 || month == 0) && (quotesPaidSummary != null && quotesPaidSummary.count >= 0)) {
            provider.getQuotesPaid(0, 0, quotesPaidSummary, large, reversed)
        } else {
            if (year <= 2021) throw IllegalArgumentException("")
            if (month !in 1..12) throw IllegalArgumentException("")
            val now = LocalDate.now()
            if (year > now.year || month > now.monthValue) throw IllegalArgumentException("")
            provider.getQuotesPaid(year = year, month = month, large = large, reversed = reversed)
        }
    }
}