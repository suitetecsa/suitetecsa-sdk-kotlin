package cu.suitetecsa.sdk.nauta.core.extensions

import java.text.SimpleDateFormat
import java.util.*

fun String.toDate(): Date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(this)