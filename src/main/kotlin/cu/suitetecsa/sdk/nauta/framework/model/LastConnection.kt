package cu.suitetecsa.sdk.nauta.framework.model


import com.google.gson.annotations.SerializedName

data class LastConnection(
    @SerializedName("from")
    val from: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("to")
    val to: String
)