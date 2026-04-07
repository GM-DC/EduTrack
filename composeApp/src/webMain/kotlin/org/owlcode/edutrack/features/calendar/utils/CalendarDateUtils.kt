package org.owlcode.edutrack.features.calendar.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Lunes de la semana que contiene [date] (dayOfWeek.ordinal = 0 para lunes) */
fun getWeekStart(date: LocalDate): LocalDate =
    date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

/** Lista de 7 fechas: lunes → domingo */
fun getWeekDays(weekStart: LocalDate): List<LocalDate> =
    (0..6).map { weekStart.plus(it, DateTimeUnit.DAY) }

/** Todos los días del mes dado */
fun getMonthDays(year: Int, month: Int): List<LocalDate> {
    val first = LocalDate(year, month, 1)
    val total = daysInMonth(year, month)
    return (0 until total).map { first.plus(it, DateTimeUnit.DAY) }
}

/** Número de días en el mes (compatible con kotlinx-datetime 0.7.x) */
internal fun daysInMonth(year: Int, month: Int): Int {
    val nextFirst = if (month == 12) LocalDate(year + 1, 1, 1)
                    else LocalDate(year, month + 1, 1)
    return nextFirst.minus(1, DateTimeUnit.DAY).day
}

/** "HH:mm" → Pair(hora, minuto); cadena vacía → (0, 0) */
fun parseHourMinute(time: String): Pair<Int, Int> {
    if (time.isBlank()) return 0 to 0
    val parts = time.split(":")
    return (parts.getOrNull(0)?.toIntOrNull() ?: 0) to
           (parts.getOrNull(1)?.toIntOrNull() ?: 0)
}

/** Minutos totales desde medianoche para "HH:mm" */
fun toMinutes(time: String): Int {
    val (h, m) = parseHourMinute(time)
    return h * 60 + m
}

