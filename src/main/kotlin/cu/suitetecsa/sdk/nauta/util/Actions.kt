package cu.suitetecsa.sdk.nauta.util

import cu.suitetecsa.sdk.nauta.core.PortalManager
import cu.suitetecsa.sdk.nauta.core.PortalManager.Connect
import cu.suitetecsa.sdk.nauta.core.PortalManager.User
import cu.suitetecsa.sdk.nauta.core.exceptions.NautaAttributeException
import cu.suitetecsa.sdk.network.Action
import cu.suitetecsa.sdk.network.HttpMethod


internal object GetCaptcha : Action {
    override fun url(): String = "/captcha/?"
    override fun ignoreContentType(): Boolean = true
    override fun timeout(): Int = 5000
}

internal data class Login(
    private val csrf: String? = null,
    private val wlanUserIp: String? = null,
    private val username: String,
    private val password: String,
    private val captchaCode: String = "",
    private val portal: PortalManager
) : Action {
    override fun url(): String = when (portal) {
        Connect -> "//LoginServlet"
        User -> "/user/login/es-es"
    }
    override fun data(): Map<String, String> = when (portal) {
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
    }
    override fun method(): HttpMethod = HttpMethod.POST
}

internal data class LoadUserInformation(
    private val username: String? = null,
    private val password: String? = null,
    private val wlanUserIp: String? = null,
    private val csrfHw: String? = null,
    private val attributeUUID: String? = null,
    private val portal: PortalManager
) : Action {
    override fun url(): String = when (portal) {
        Connect -> "${portal.baseUrl}/EtecsaQueryServlet"
        User -> "${portal.baseUrl}/useraaa/user_info"
    }
    override fun data(): Map<String, String>? = when (portal) {
        Connect -> attributeUUID?.let {
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
    }
    override fun method(): HttpMethod = HttpMethod.POST
}

internal data class Recharge(private val csrf: String? = null, private val rechargeCode: String) : Action {
    override fun url(): String = "${User.baseUrl}/useraaa/recharge_account"
    override fun data(): Map<String, String> = mutableMapOf("csrf" to run { csrf ?: "" }, "recharge_code" to rechargeCode, "btn_submit" to "")
    override fun method(): HttpMethod = HttpMethod.POST
}

internal data class Transfer(
    private val csrf: String? = null,
    private val amount: Float,
    private val password: String,
    private val destinationAccount: String?
) : Action {
    override fun url(): String = "${User.baseUrl}${destinationAccount?.let { " / useraaa / transfer_balance" } ?: "/useraaa/transfer_nautahogarpaid"}"
    override fun data(): Map<String, String> = destinationAccount?.let {
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
    )
    override fun method(): HttpMethod = HttpMethod.POST
}

internal data class ChangePassword(
    private val csrf: String? = null,
    private val oldPassword: String,
    private val newPassword: String,
    private val changeMail: Boolean = false
) : Action {
    override fun url(): String = "${User.baseUrl}${if (changeMail) "/mail/change_password" else "/Useraaa/change_password"}"
    override fun data(): Map<String, String> = mutableMapOf(
        "csrf" to run { csrf ?: "" },
        "old_password" to oldPassword,
        "new_password" to newPassword,
        "repeat_new_password" to newPassword,
        "btn_submit" to ""
    )
    override fun method(): HttpMethod = HttpMethod.POST
}

internal data class GetSummary(
    private val csrf: String? = null,
    private val year: Int,
    private val month: Int,
    private val type: ActionType
) : Action {
    override fun url(): String = User.baseUrl + when (type) {
        ActionType.Connections -> "/useraaa/service_detail_summary/"
        ActionType.Recharges -> "/useraaa/recharge_detail_summary/"
        ActionType.Transfers -> "/useraaa/transfer_detail_summary/"
        ActionType.QuotesPaid -> "/useraaa/nautahogarpaid_detail_summary/"
    }
    override fun data(): Map<String, String> = mutableMapOf(
        "csrf" to run { csrf ?: "" },
        "year_month" to "$year-${String.format("%02d", month)}",
        "list_type" to when (type) {
            ActionType.Connections -> "service_detail"
            ActionType.Recharges -> "recharge_detail"
            ActionType.Transfers -> "transfer_detail"
            ActionType.QuotesPaid -> "nautahogarpaid_detail"
        }
    )
    override fun method(): HttpMethod = HttpMethod.POST
    override fun csrfUrl(): String =  User.baseUrl + when (type) {
        ActionType.Connections -> "/useraaa/service_detail/"
        ActionType.Recharges -> "/useraaa/recharge_detail/"
        ActionType.Transfers -> "/useraaa/transfer_detail/"
        ActionType.QuotesPaid -> "/useraaa/nautahogarpaid_detail/"
    }
}

internal data class GetActions(
    val count: Int,
    val yearMonthSelected: String,
    val large: Int,
    val reversed: Boolean,
    val type: ActionType
) : Action {
    override fun url(): String = User.baseUrl + when (type) {
        ActionType.Connections -> "/useraaa/service_detail_list/"
        ActionType.Recharges -> "/useraaa/recharge_detail_list/"
        ActionType.Transfers -> "/useraaa/transfer_detail_list/"
        ActionType.QuotesPaid -> "/useraaa/nautahogarpaid_detail_list/"
    }

    override fun count(): Int = count
    override fun yearMonthSelected(): String = yearMonthSelected
    override fun large(): Int = large
    override fun reversed(): Boolean = reversed
    override fun type(): ActionType = type
}

internal data class Logout(
    private val username: String,
    private val wlanUserIp: String,
    private val csrfHw: String,
    private val attributeUUID: String
) : Action {
    override fun url(): String = "${Connect.baseUrl}/LogoutServlet"
    override fun data(): Map<String, String> = mutableMapOf(
        "username" to username,
        "wlanuserip" to wlanUserIp,
        "CSRFHW" to csrfHw,
        "ATTRIBUTE_UUID" to attributeUUID
    )
}

internal data class CheckConnection(private val url: String? = null) : Action {
    override fun url(): String = url ?: "http://www.cubadebate.cu/"
}

internal data class GetPage(private val url: String, private val dataMap: Map<String, String>) : Action {
    override fun url(): String = url
    override fun data(): Map<String, String> = dataMap
    override fun method(): HttpMethod = HttpMethod.POST
}

enum class ActionType {
    Connections,
    Recharges,
    Transfers,
    QuotesPaid
}