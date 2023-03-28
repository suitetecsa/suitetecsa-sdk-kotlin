package cu.suitetecsa.sdk.nauta.service

import cu.suitetecsa.sdk.nauta.utils.Action
import org.jsoup.nodes.Document

interface NautaProvider {

    fun getCredentials(): Pair<String, String>
    fun setCredentials(userName: String, password: String)
    fun getDataSession(): Map<String, String>
    fun loadDataSession(dataSession: Map<String, String>)
    fun getCaptcha(): ByteArray
    fun init()
    fun getInformationConnect(): Map<String, Any>
    fun getRemainingTime(): String
    fun getInformationUser(soup: Document? = null): Map<String, Map<String, String>>
    fun connect()
    fun disconnect()
    fun login(captchaCode: String): Map<String, Map<String, String>>
    fun toUpBalance(rechargeCode: String)
    fun transferFunds(amount: Float, destinationAccount: String? = null)
    fun changePassword(newPassword: String)
    fun changeEmailPassword(oldPassword: String, newPassword: String)
    fun getConnectionsSummary(year: Int, month: Int): Map<String, Map<String, Any>>
    fun getConnections(
        year: Int,
        month: Int,
        summary: Map<String, Any>? = null,
        large: Int = 0,
        reversed: Boolean = false
    ): Map<String, Any>

    fun getRechargesSummary(year: Int, month: Int): Map<String, Map<String, Any>>
    fun getRecharges(
        year: Int,
        month: Int,
        summary: Map<String, Any>? = null,
        large: Int = 0,
        reversed: Boolean = false
    ): Map<String, Any>

    fun getTransfersSummary(year: Int, month: Int): Map<String, Map<String, Any>>
    fun getTransfers(
        year: Int,
        month: Int,
        summary: Map<String, Any>? = null,
        large: Int = 0,
        reversed: Boolean = false
    ): Map<String, Any>

    fun getQuotesPaidSummary(year: Int, month: Int): Map<String, Map<String, Any>>
    fun getQuotesPaid(
        year: Int,
        month: Int,
        summary: Map<String, Any>? = null,
        large: Int = 0,
        reversed: Boolean = false
    ): Map<String, Any>

    fun getLasts(action: Action, large: Int = 5): Map<String, MutableList<Map<String, Any>>>
}