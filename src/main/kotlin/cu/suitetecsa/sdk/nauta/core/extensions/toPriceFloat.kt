package cu.suitetecsa.sdk.nauta.core.extensions

fun String.toPriceFloat(): Float = this
        .replace("$", "")
        .replace(",", ".")
        .replace(" CUP", "")
        .replace(" ", "")
        .toFloatOrNull() ?: throw IllegalArgumentException("El formato de la cadena no es correcto")