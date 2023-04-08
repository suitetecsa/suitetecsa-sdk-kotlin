package cu.suitetecsa.sdk.nauta.domain.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun timeStringToSeconds(timeString: String) =
    timeString.split(":").map { it.toInt() }.fold(0) { acc, value -> acc * 60 + value }

fun secondsToTimeString(seconds: Int) = "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)

fun parseDateTime(dateTimeString: String): LocalDateTime =
    LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

fun formatDateTime(dateTime: LocalDateTime): String =
    dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

fun stringToDate(dateString: String): Date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateString)

fun dateToString(date: Date): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
