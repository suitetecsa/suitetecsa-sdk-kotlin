package cu.suitetecsa.sdk.nauta.domain.model

import java.util.Date


data class Connection(
    val startSession: Date,
    val endSession: Date,
    val duration: Int,
    val uploaded: Double,
    val downloaded: Double,
    val import: Float
)
