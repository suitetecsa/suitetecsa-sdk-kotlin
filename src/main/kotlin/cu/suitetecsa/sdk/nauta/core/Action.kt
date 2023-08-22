package cu.suitetecsa.sdk.nauta.core

import cu.suitetecsa.sdk.nauta.core.ActionType.*
import cu.suitetecsa.sdk.nauta.core.HttpMethod.GET
import cu.suitetecsa.sdk.nauta.core.HttpMethod.POST
import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import cu.suitetecsa.sdk.nauta.core.exceptions.NautaAttributeException

sealed class Action(
    val route: String,
    val data: Map<String, String>? = null,
    val method: HttpMethod = GET,
    val portalManager: PortalManager,
    val ignoreContentType: Boolean = false,
    val timeout: Int = 30000,
    val csrfUrl: String? = null
) {
    object GetCaptcha : Action(
        "/captcha/?",
        portalManager = User,
        ignoreContentType = true,
        timeout = 5000
    )
    data class Login(
        private val csrf: String? = null,
        private val wlanUserIp: String? = null,
        private val username: String,
        private val password: String,
        private val captchaCode: String = "",
        private val portal: PortalManager
    ) : Action(
        route = when (portal) {
            Connect -> "//LoginServlet"
            User -> "/user/login/es-es"
        },
        data = when (portal) {
            Connect -> mapOf(
                "CSRFHW" to run { csrf ?: throw NautaAttributeException("CSRFHW is required") },
                "wlanuserip" to run { wlanUserIp ?: throw NautaAttributeException("wlanuserip is required") },
                "username" to username,
                "password" to password
            )
            User -> mapOf(
                "csrf" to run { csrf ?: throw NautaAttributeException("csrf is required") },
                "login_user" to username,
                "password_user" to password,
                "captcha" to captchaCode,
                "btn_submit" to ""
            )
        },
        method = POST,
        portalManager = portal
    )

    data class LoadUserInformation(
        private val username: String? = null,
        private val password: String? = null,
        private val wlanUserIp: String? = null,
        private val csrfHw: String? = null,
        private val attributeUUID: String? = null,
        private val portal: PortalManager
    ) : Action(
        route = when (portal) {
            Connect -> "/EtecsaQueryServlet"
            User -> "/useraaa/user_info"
        },
        data = when (portal) {
            Connect -> attributeUUID ?.let {
                mapOf(
                    "op" to "getLeftTime",
                    "ATTRIBUTE_UUID" to it,
                    "CSRFHW" to run { csrfHw ?: throw NautaAttributeException("csrfHw is required") },
                    "wlanuserip" to run { wlanUserIp ?: throw NautaAttributeException("wlanUserIp is required") },
                    "username" to run { username ?: throw NautaAttributeException("username is required") }
                )
            } ?: mapOf(
                "username" to run { username ?: throw NautaAttributeException("username is required") },
                "password" to run { password ?: throw NautaAttributeException("password is required") },
                "wlanuserip" to run { wlanUserIp ?: throw NautaAttributeException("wlanUserIp is required") },
                "CSRFHW" to run { csrfHw ?: throw NautaAttributeException("csrfHw is required") },
                "lang" to ""
            )
            User -> null
        },
        method = POST,
        portalManager = portal
    )
    data class Recharge(private val csrf: String? = null, private val rechargeCode: String) : Action(
        route = "/useraaa/recharge_account",
        data = mutableMapOf("csrf" to run { csrf ?: "" }, "recharge_code" to rechargeCode, "btn_submit" to ""),
        method = POST,
        portalManager = User
    )

    data class Transfer(
        private val csrf: String? = null,
        private val amount: Float,
        private val password: String,
        private val destinationAccount: String?
    ) : Action(
        route = destinationAccount?.let { "/useraaa/transfer_balance" } ?: "/useraaa/transfer_nautahogarpaid",
        data = destinationAccount?.let {
            mutableMapOf(
                "csrf" to run { csrf ?: "" },
                "transfer" to String.format("%.2f", amount).replace(".", ","),
                "id_cuenta" to it,
                "password_user" to password,
                "action" to "checkdata"
            )
        } ?: mutableMapOf(
            "csrf" to run { csrf ?: "" },
            "transfer" to String.format("%.2f", amount).replace(".", ","),
            "password_User" to password,
            "action" to "checkdata"
        ),
        method = POST,
        portalManager = User
    )

    data class ChangePassword(
        private val csrf: String? = null,
        private val oldPassword: String,
        private val newPassword: String,
        private val changeMail: Boolean = false
    ) :
        Action(
            route = if (changeMail) "/mail/change_password" else "/Useraaa/change_password",
            data = mutableMapOf(
                "csrf" to run { csrf ?: "" },
                "old_password" to oldPassword,
                "new_password" to newPassword,
                "repeat_new_password" to newPassword,
                "btn_submit" to ""
            ),
            method = POST,
            portalManager = User
        )

    data class GetSummary(
        private val csrf: String? = null,
        private val year: Int,
        private val month: Int,
        private val type: ActionType
    ) : Action(
        route = when (type) {
            Connections -> "/useraaa/service_detail_summary/"
            Recharges -> "/useraaa/recharge_detail_summary/"
            Transfers -> "/useraaa/transfer_detail_summary/"
            QuotesPaid -> "/useraaa/nautahogarpaid_detail_summary/"
        },
        data = mutableMapOf(
            "csrf" to run { csrf ?: "" }, "year_month" to "$year-${String.format("%02d", month)}", "list_type" to when (type) {
                Connections -> "service_detail"
                Recharges -> "recharge_detail"
                Transfers -> "transfer_detail"
                QuotesPaid -> "nautahogarpaid_detail"
            }
        ),
        method = POST,
        portalManager = User,
        csrfUrl = when (type) {
            Connections -> "/useraaa/service_detail/"
            Recharges -> "/useraaa/recharge_detail/"
            Transfers -> "/useraaa/transfer_detail/"
            QuotesPaid -> "/useraaa/nautahogarpaid_detail/"
        }
    )

    data class GetActions(
        val count: Int,
        val yearMonthSelected: String,
        val large: Int,
        val reversed: Boolean,
        val type: ActionType
    ) : Action(
        route = when (type) {
            Connections -> "/useraaa/service_detail_list/"
            Recharges -> "/useraaa/recharge_detail_list/"
            Transfers -> "/useraaa/transfer_detail_list/"
            QuotesPaid -> "/useraaa/nautahogarpaid_detail_list/"
        },
        portalManager = User
    )
    data class Logout(
        private val username: String,
        private val wlanUserIp: String,
        private val csrfHw: String,
        private val attributeUUID: String
    ) : Action(
        route = "/LogoutServlet",
        data = mutableMapOf(
            "username" to username,
            "wlanuserip" to wlanUserIp,
            "CSRFHW" to csrfHw,
            "ATTRIBUTE_UUID" to attributeUUID
        ),
        portalManager = Connect
    )
    data class CheckConnection(private val url: String? = null) : Action(
        route = url ?: "http://www.cubadebate.cu/",
        portalManager = Connect
    )

    data class GetPage(private val url: String, private val dataMap: Map<String, String>) : Action(
        route = url,
        data = dataMap,
        method = POST,
        portalManager = Connect
    )
}

enum class ActionType {
    Connections,
    Recharges,
    Transfers,
    QuotesPaid
}