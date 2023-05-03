package cu.suitetecsa.sdk.nauta.data.model

data class NautaConnectUser(
    val accountInfo: AccountInfo,
    val lastsConnections: List<LastsConnection>
)