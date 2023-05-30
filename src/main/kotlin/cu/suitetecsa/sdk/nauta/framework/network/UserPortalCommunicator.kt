package cu.suitetecsa.sdk.nauta.framework.network

import cu.suitetecsa.sdk.nauta.core.Action
import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.domain.model.*

interface UserPortalCommunicator {
    var csrf: String
    var isNautaHome: Boolean
    val captchaImage: ByteArray
    val userInformation: ResultType<String?>

    fun loadCsrf(action: Action): ResultType<String?>
    fun login(username: String, password: String, captchaCode: String): ResultType<String?>
    fun topUp(rechargeCode: String): ResultType<String?>
    fun transferFunds(amount: Float, password: String, destinationAccount: String? = null): ResultType<String?>
    fun changePassword(oldPassword: String, newPassword: String): ResultType<String?>
    fun changeEmailPassword(oldPassword: String, newPassword: String): ResultType<String?>
    fun getConnectionsSummary(year: Int, month: Int): ResultType<String?>
    fun getRechargesSummary(year: Int, month: Int): ResultType<String?>
    fun getTransfersSummary(year: Int, month: Int): ResultType<String?>
    fun getQuotesPaidSummary(year: Int, month: Int): ResultType<String?>
    fun getConnections(connectionsSummary: ConnectionsSummary, large: Int = 0, reversed: Boolean = false): ResultType<List<String?>>
    fun getRecharges(rechargesSummary: RechargesSummary, large: Int = 0, reversed: Boolean = false): ResultType<List<String?>>
    fun getTransfers(transfersSummary: TransfersSummary, large: Int = 0, reversed: Boolean = false): ResultType<List<String?>>
    fun getQuotesPaid(quotesPaidSummary: QuotesPaidSummary, large: Int = 0, reversed: Boolean = false): ResultType<List<String?>>
}