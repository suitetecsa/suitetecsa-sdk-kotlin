package cu.suitetecsa.sdk.nauta.domain.util

import java.text.NumberFormat
import java.util.*

fun priceStringToFloat(priceString: String) = priceString
    .replace("$", "")
    .replace(",", ".")
    .replace(" CUP", "")
    .toFloatOrNull() ?: throw IllegalArgumentException("El formato de la cadena no es correcto")

fun floatToPriceString(price: Float): String = NumberFormat
    .getCurrencyInstance(Locale.US)
    .format(price)