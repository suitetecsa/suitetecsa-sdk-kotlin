package cu.suitetecsa.sdk.nauta.data.repository

import cu.suitetecsa.sdk.nauta.domain.model.*
import org.jsoup.nodes.Document

interface NautaScrapper {
    val isNautaHome: Boolean
    val isLoggedIn: Boolean
    val isUserLoggedIn: Boolean
    val isConnected: Boolean

    fun getDataSession(): Map<String, String>
    fun loadDataSession(dataSession: Map<String, String>)
    fun getCaptcha(): ByteArray
    fun init()
    fun getInformationConnect(userName: String, password: String): Map<String, Any>
    fun getRemainingTime(): String
    fun getInformationUser(soup: Document? = null): NautaUser
    fun connect(userName: String, password: String)
    fun disconnect()
    fun login(userName: String, password: String, captchaCode: String): NautaUser
    fun toUpBalance(rechargeCode: String)
    fun transferFunds(amount: Float, password: String, destinationAccount: String? = null)
    fun changePassword(oldPassword: String, newPassword: String)
    fun changeEmailPassword(oldPassword: String, newPassword: String)
    fun getConnectionsSummary(year: Int, month: Int): ConnectionsSummary
    fun getConnections(
        year: Int, month: Int, summary: ConnectionsSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Connection>

    fun getRechargesSummary(year: Int, month: Int): RechargesSummary
    fun getRecharges(
        year: Int, month: Int, summary: RechargesSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Recharge>

    fun getTransfersSummary(year: Int, month: Int): TransfersSummary
    fun getTransfers(
        year: Int, month: Int, summary: TransfersSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<Transfer>

    fun getQuotesPaidSummary(year: Int, month: Int): QuotesPaidSummary
    fun getQuotesPaid(
        year: Int, month: Int, summary: QuotesPaidSummary? = null, large: Int = 0, reversed: Boolean = false
    ): List<QuotePaid>
}