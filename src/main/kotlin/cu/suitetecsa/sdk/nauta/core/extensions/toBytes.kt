package cu.suitetecsa.sdk.nauta.core.extensions

import java.util.*

fun String.toBytes(): Double {
    val sizeUnit = this.split(" ").last()
    val sizeValue = this.replace(" $sizeUnit", "").replace(" ", "")
    return sizeValue.replace(",", ".").toDouble() * when (sizeUnit.uppercase(Locale.getDefault())) {
        "KB" -> 1024
        "MB" -> 1024 * 1024
        "GB" -> 1024 * 1024 * 1024
        else -> throw IllegalArgumentException("La unidad de tamaño no es válida")
    }.toLong()
}