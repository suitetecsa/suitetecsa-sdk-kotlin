package cu.suitetecsa.sdk.nauta.core.extensions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a string representation of a date to a [SimpleDateFormat] object.
 *
 * @return The parsed [SimpleDateFormat] object.
 */
fun String.toDate(): Date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(this)
