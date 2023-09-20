package cu.suitetecsa.sdk.network

import cu.suitetecsa.sdk.nauta.util.ActionType
import cu.suitetecsa.sdk.network.HttpMethod.GET

interface Action {
    fun url(): String
    fun data(): Map<String, String>? = null
    fun method(): HttpMethod = GET
    fun ignoreContentType(): Boolean = false
    fun timeout(): Int = 30000
    fun csrfUrl(): String? = null

    fun count(): Int = 0
    fun yearMonthSelected(): String = ""
    fun large(): Int = 0
    fun reversed(): Boolean = false
    fun type(): ActionType = ActionType.Connections
}