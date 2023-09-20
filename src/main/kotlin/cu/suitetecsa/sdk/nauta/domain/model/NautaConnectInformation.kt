package cu.suitetecsa.sdk.nauta.domain.model


import com.google.gson.annotations.SerializedName

data class NautaConnectInformation(
    @SerializedName("account_info")
    val accountInfo: AccountInfo,
    @SerializedName("lasts_connections")
    val lastConnections: List<LastConnection>
)