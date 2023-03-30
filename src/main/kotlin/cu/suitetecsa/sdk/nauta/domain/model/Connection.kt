package cu.suitetecsa.sdk.nauta.domain.model

import java.time.LocalDateTime

data class Connection(
    val startSession: LocalDateTime,
    val endSession: LocalDateTime,
    val duration: Int,
    val uploaded: Double,
    val downloaded: Double,
    val import: Float
)
