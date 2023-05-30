package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.framework.model.ResultType
import cu.suitetecsa.sdk.nauta.domain.model.*

interface UserPortalScraper {
    fun parseErrors(html: String): ResultType<String>
    fun parseCsrfToken(html: String): String
    fun parseNautaUser(html: String): NautaUser
    fun parseConnectionsSummary(html: String): ConnectionsSummary
    fun parseRechargesSummary(html: String): RechargesSummary
    fun parseTransfersSummary(html: String): TransfersSummary
    fun parseQuotesPaidSummary(html: String): QuotesPaidSummary
    fun parseConnections(html: String): List<Connection>
    fun parseRecharges(html: String): List<Recharge>
    fun parseTransfers(html: String): List<Transfer>
    fun parseQuotesPaid(html: String): List<QuotePaid>
}