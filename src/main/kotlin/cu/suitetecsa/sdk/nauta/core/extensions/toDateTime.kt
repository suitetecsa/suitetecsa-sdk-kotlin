package cu.suitetecsa.sdk.nauta.core.extensions

import java.text.SimpleDateFormat
import java.util.*

fun String.toDateTime(): Date = try {
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(this)
} catch (e: Exception) {
    Date(0)
}