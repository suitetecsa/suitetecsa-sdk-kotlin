package cu.suitetecsa.sdk.nauta.utils

val userAgent: String
    get() = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:97.0) Gecko/20100101 Firefox/97.0"
val headers: MutableMap<String, String>
    get() = mutableMapOf(
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Encoding" to "gzip, deflate, br",
        "Accept-Language" to "es-MX,es;q=0.8,en-US;q=0.5,en;q=0.3"
    )
val connectDomain: String
    get() = "secure.etecsa.net"
val urlBase: Map<Portal, String>
    get() = mapOf(
        Portal.CONNECT to "https://${connectDomain}:8443/",
        Portal.USER to "https://www.portal.nauta.cu/"
    )
val portalsUrls: Map<Portal, Map<Action, Any>>
    get() = mapOf(
        Portal.CONNECT to mapOf(
            Action.LOGOUT to "LogoutServlet",
            Action.LOAD_USER_INFORMATION to "EtecsaQueryServlet",
            Action.CHECK_CONNECTION to "http://www.cubadebate.cu/"
        ),
        Portal.USER to mapOf(
            Action.LOGIN to "user/login/es-es",
            Action.LOAD_USER_INFORMATION to "useraaa/user_info",
            Action.RECHARGE to "useraaa/recharge_account",
            Action.TRANSFER to "useraaa/transfer_balance",
            Action.NAUTA_HOGAR_PAID to "useraaa/transfer_nautahogarpaid",
            Action.CHANGE_PASSWORD to "useraaa/change_password",
            Action.CHANGE_EMAIL_PASSWORD to "mail/change_password",
            Action.GET_CONNECTIONS to mapOf(
                "base" to "useraaa/service_detail/",
                "summary" to "useraaa/service_detail_summary/",
                "list" to "useraaa/service_detail_list/"
            ),
            Action.GET_RECHARGES to mapOf(
                "base" to "useraaa/recharge_detail/",
                "summary" to "useraaa/recharge_detail_summary/",
                "list" to "useraaa/recharge_detail_list/"
            ),
            Action.GET_TRANSFERS to mapOf(
                "base" to "useraaa/transfer_detail/",
                "summary" to "useraaa/transfer_detail_summary/",
                "list" to "useraaa/transfer_detail_list/",
            ),
            Action.GET_QUOTES_PAID to mapOf(
                "base" to "useraaa/nautahogarpaid_detail/",
                "summary" to "useraaa/nautahogarpaid_detail_summary/",
                "list" to "useraaa/nautahogarpaid_detail_list/"
            ),
            Action.LOGOUT to "user/logout"
        )
    )
