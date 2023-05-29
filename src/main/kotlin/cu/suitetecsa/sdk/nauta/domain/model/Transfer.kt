package cu.suitetecsa.sdk.nauta.domain.model

import java.util.*

data class Transfer(
    val date: Date,
    val import: Float,
    val destinyAccount: String
)
