package cu.suitetecsa.sdk.nauta.domain.model

import java.util.Date

data class NautaUser(
    val userName: String,
    val blockingDate: String,
    val dateOfElimination: String,
    val accountType: String,
    val serviceType: String,
    val credit: String,
    val time: String,
    val mailAccount: String,
    val offer: String? = null,
    val monthlyFee: String? = null,
    val downloadSpeed: String? = null,
    val uploadSpeed: String? = null,
    val phone: String? = null,
    val linkIdentifiers: String? = null,
    val linkStatus: String? = null,
    val activationDate: String? = null,
    val blockingDateHome: String? = null,
    val dateOfEliminationHome: String? = null,
    val quotePaid: String? = null,
    val voucher: String? = null,
    val debt: String? = null
)
