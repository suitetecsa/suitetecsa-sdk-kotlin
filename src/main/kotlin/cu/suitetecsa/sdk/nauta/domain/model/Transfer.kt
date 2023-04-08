package cu.suitetecsa.sdk.nauta.domain.model

import java.time.LocalDateTime

data class Transfer(
    val date: LocalDateTime,
    val import: Float,
    val destinyAccount: String
)
