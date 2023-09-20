package cu.suitetecsa.sdk.nauta.core.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.toDateTimeString(): String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(this)