package cu.suitetecsa.sdk.nauta.core.extensions

import java.util.*

/**
 * Converts a string representation of a size to bytes.
 *
 * @return The size in bytes.
 * @throws IllegalArgumentException if the size unit is not valid.
 */
fun String.toBytes(): Double {
    val sizeUnit = this.split(" ").last()
    val sizeValue = this.replace(" $sizeUnit", "").replace(" ", "")
    return convertToBytes(sizeValue, sizeUnit.uppercase(Locale.getDefault()))
}

private fun convertToBytes(sizeValue: String, sizeUnit: String): Double {
    return sizeValue.replace(",", ".").toDouble() * when (sizeUnit) {
        "KB" -> 1024
        "MB" -> 1024 * 1024
        "GB" -> 1024 * 1024 * 1024
        else -> throw IllegalArgumentException("La unidad de tamaño no es válida")
    }.toLong()
}