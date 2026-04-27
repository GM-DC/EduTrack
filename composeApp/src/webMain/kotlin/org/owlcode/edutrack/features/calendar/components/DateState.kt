package org.owlcode.edutrack.features.calendar.components

import kotlinx.datetime.LocalDate

private val DOW_ABREV_ES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val MES_ABREV_ES = listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                       "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

/** Año máximo permitido en el selector de fechas. */
const val MAX_YEAR = 2100

data class DateState(
    val day: Int   = 1,
    val month: Int = 1,
    val year: Int  = 2026
)

/** Número de días del mes de este estado (respeta años bisiestos). */
fun DateState.daysInMonth(): Int = when (month) {
    2           -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else        -> 31
}

fun DateState.toDisplayString(): String {
    val d = day.toString().padStart(2, '0')
    val m = month.toString().padStart(2, '0')
    return "$d / $m / $year"
}

fun DateState.toFriendlyString(): String {
    if (!isValid()) return "Fecha invalida"
    return runCatching {
        val date = LocalDate(year, month, day)
        val dow  = DOW_ABREV_ES[date.dayOfWeek.ordinal]
        val mes  = MES_ABREV_ES[month]
        "$dow, $day $mes $year"
    }.getOrDefault(toDisplayString())
}

fun DateState.toIsoString(): String {
    val d = day.toString().padStart(2, '0')
    val m = month.toString().padStart(2, '0')
    return "$year-$m-$d"
}

fun DateState.isValid(): Boolean {
    if (month !in 1..12 || day < 1 || year !in 1900..MAX_YEAR) return false
    return day <= daysInMonth()
}

fun String.toDateState(): DateState? {
    if (isBlank()) return null
    val parts = trim().split("-")
    if (parts.size != 3) return null
    val year  = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day   = parts[2].toIntOrNull() ?: return null
    return DateState(day = day, month = month, year = year)
}