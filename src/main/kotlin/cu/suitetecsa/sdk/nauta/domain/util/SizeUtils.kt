package cu.suitetecsa.sdk.nauta.domain.util

import java.util.*

fun sizeStringToBytes(sizeString: String): Double {
    val (sizeValue, sizeUnit) = sizeString.split(" ")
    return sizeValue.replace(",", ".").toDouble() * when (sizeUnit.uppercase(Locale.getDefault())) {
        "KB" -> 1024
        "MB" -> 1024 * 1024
        "GB" -> 1024 * 1024 * 1024
        else -> throw IllegalArgumentException("La unidad de tamaño no es válida")
    }.toLong()
}

fun bytesToSizeString(bytes: Long): String {
    val sizeUnits = arrayOf("bytes", "KB", "MB", "GB", "TB")
    var sizeValue = bytes.toDouble()
    var sizeUnitIndex = 0
    while (sizeValue >= 1024 && sizeUnitIndex < sizeUnits.lastIndex) {
        sizeValue /= 1024
        sizeUnitIndex++
    }
    return "%.2f %s".format(sizeValue).replace(".", ",") + " " + sizeUnits[sizeUnitIndex]
}