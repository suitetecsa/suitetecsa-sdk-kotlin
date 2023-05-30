package cu.suitetecsa.sdk.nauta.core

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

internal fun Connection.Response.throwExceptionOnFailure(exc: Class<out Exception>, msg: String) {
    val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
    if (this.statusCode() != 200 && this.statusCode() != 302) {
        throw exceptionConstructor.newInstance(
            "$msg :: ${this.statusMessage()}"
        )
    }
}

internal fun Document.throwExceptionOnFailure(exc: Class<out Exception>, msg: String, portalManager: Portal) {
    val exceptionConstructor = exc.getDeclaredConstructor(String::class.java)
    val errors = mutableListOf<String>()
        val lastScript = this.select("script[type='text/javascript']").last()
        if (lastScript != null) {
            val failReason = mapOf(
                Portal.CONNECT to """alert\("(?<reason>[^"]*?)"\)""",
                Portal.USER to """toastr\.error\('(?<reason>.*)'\)"""
            )
            val regex = Regex(failReason[portalManager]!!)
            val reason = regex.find(lastScript.data().trim())?.groups?.get("reason")?.value
            if (portalManager == Portal.CONNECT && reason != null) {
                throw exceptionConstructor.newInstance(
                    "$msg :: $reason"
                )
            } else {
                val error = reason?.let { Jsoup.parse(it).selectFirst("li[class=\"msg_error\"]") }
                if (error != null) {
                    if (error.text().startsWith("Se han detectado algunos errores.")) {
                        val subMessages = error.select("li[class='sub-message']")
                        for (subMessage in subMessages) {
                            errors.add(subMessage.text())
                        }
                        throw exceptionConstructor.newInstance(
                            "$msg :: $errors"
                        )
                    } else {
                        throw exceptionConstructor.newInstance(
                            "$msg :: ${error.text()}"
                        )
                    }
                }
            }
        }
}

internal operator fun Elements.component6(): Element {
    return this[5]
}

fun String.toSeconds() = this.split(":").fold(0L) { acc, s -> acc * 60 + s.toLong() }

fun Long.toTimeString() = String.format("%02d:%02d:%02d", this / 3600, this % 3600 / 60, this % 60)

fun String.toDate(): Date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }.parse(this)

fun Date.toDateString(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)

fun String.toDateTime(): Date = try {
    SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(this)
} catch (e: Exception) {
    Date(0)
}

fun Date.toDateTimeString(): String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(this)

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

fun Long.toSizeString(): String {
    val sizeUnits = arrayOf("bytes", "KB", "MB", "GB", "TB")
    var sizeValue = this.toDouble()
    var sizeUnitIndex = 0
    while (sizeValue >= 1024 && sizeUnitIndex < sizeUnits.lastIndex) {
        sizeValue /= 1024
        sizeUnitIndex++
    }
    return "%.2f %s".format(sizeValue).replace(".", ",") + " " + sizeUnits[sizeUnitIndex]
}

fun String.toPriceFloat(): Float = this
        .replace("$", "")
        .replace(",", ".")
        .replace(" CUP", "")
        .replace(" ", "")
        .toFloatOrNull() ?: throw IllegalArgumentException("El formato de la cadena no es correcto")

fun Float.toPriceString(): String = NumberFormat
    .getCurrencyInstance(Locale.US)
    .format(this)