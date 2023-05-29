package cu.suitetecsa.sdk.nauta.core

internal fun makeUrl(
    action: Action,
    portalManager: Portal,
    getAction: Boolean = false,
    subAction: String? = null,
    yearMonthSelected: String? = null,
    count: Int? = null,
    page: Int? = null
): String {
    if (action == Action.CHECK_CONNECTION) {
        return portalsUrls[portalManager]!![action] as String
    } else if (!getAction) {
        return "${urlBase[portalManager]}${portalsUrls[portalManager]!![action]}"
    } else {
        val url = "${urlBase[portalManager]}${(portalsUrls[portalManager]!![action]!! as Map<*, *>)[subAction]}"
        when (subAction) {
            "base", "summary" -> {
                return url
            }

            "list" -> {
                if (yearMonthSelected.isNullOrEmpty()) {
                    throw Exception("yearMonthSelected is required")
                }
                if (count == null) {
                    throw Exception("count is required")
                }
                return if (page == null) {
                    "${url}${yearMonthSelected}/${count}"
                } else "${url}${yearMonthSelected}/${count}/${page}"
            }
        }
    }
    return ""
}