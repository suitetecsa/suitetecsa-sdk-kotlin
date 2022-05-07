package cu.suitetecsa.kotlibsuitetecsa.models

data class Connection(
    val startSession: String,
    val endSession: String,
    val duration: String,
    val upload: String,
    val download: String,
    val import_: String
)
