package cu.suitetecsa.sdk.nauta.framework

import cu.suitetecsa.sdk.nauta.framework.model.NautaConnectInformation
import cu.suitetecsa.sdk.nauta.framework.model.ResultType

interface ConnectPortalScraper {
    fun parseErrors(html: String): ResultType<String>
    fun parseCheckConnections(html: String): Boolean
    fun parseActionForm(html: String): Pair<String, Map<String, String>>
    fun parseLoginForm(html: String): Pair<String, Map<String, String>>
    fun parseNautaConnectInformation(html: String): NautaConnectInformation
    fun parseRemainingTime(html: String): Long
    fun parseAttributeUUID(html: String): String
    fun isSuccessLogout(html: String): Boolean
}