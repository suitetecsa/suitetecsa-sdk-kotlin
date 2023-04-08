package cu.suitetecsa.sdk.nauta.domain.model

data class ConnectionsSummary(
    val count: Int,
    val yearMonthSelected: String,
    val totalTime: Int,
    val totalImport: Float,
    val uploaded: Double,
    val downloaded: Double,
    val totalTraffic: Double
)
