package cu.suitetecsa.sdk.nauta.domain.model

import java.time.LocalDateTime

data class QuotePaid(
    val date: LocalDateTime,
    val import: Float,
    val channel: String,
    val type: String,
    val office: String
)
