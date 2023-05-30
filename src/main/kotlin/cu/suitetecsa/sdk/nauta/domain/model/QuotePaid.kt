package cu.suitetecsa.sdk.nauta.domain.model

import java.util.*

data class QuotePaid(
    val date: Date,
    val import: Float,
    val channel: String,
    val type: String,
    val office: String
)
