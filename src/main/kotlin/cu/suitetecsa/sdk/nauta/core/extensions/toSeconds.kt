package cu.suitetecsa.sdk.nauta.core.extensions

fun String.toSeconds() = this.split(":").fold(0L) { acc, s -> acc * 60 + s.toLong() }