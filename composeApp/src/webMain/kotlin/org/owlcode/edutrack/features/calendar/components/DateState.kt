package org.owlcode.edutrack.features.calendar.components

/**
 * Estado interno del DatePicker.
 * [day] de 1 a 31; [month] de 1 a 12; [year] ≥ 1900.
 */
data class DateState(
    val day: Int   = 1,
    val month: Int = 1,
    val year: Int  = 2026
)

/** Muestra la fecha en formato visual "DD / MM / YYYY". */
fun DateState.toDisplayString(): String {
    val d = day.toString().padStart(2, '0')
    val m = month.toString().padStart(2, '0')
    return "$d / $m / $year"
}

/** Convierte al formato de almacenamiento "YYYY-MM-DD". */
fun DateState.toIsoString(): String {
    val d = day.toString().padStart(2, '0')
    val m = month.toString().padStart(2, '0')
    return "$year-$m-$d"
}

/** Valida si el estado representa una fecha calendario posible. */
fun DateState.isValid(): Boolean {
    if (month !in 1..12 || day < 1 || year < 1900) return false
    val maxDay = when (month) {
        2    -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day <= maxDay
}

/**
 * Parsea una cadena "YYYY-MM-DD" a [DateState].
 * Devuelve null si el formato es inválido o la cadena está vacía.
 */
fun String.toDateState(): DateState? {
    if (isBlank()) return null
    val parts = trim().split("-")
    if (parts.size != 3) return null
    val year  = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day   = parts[2].toIntOrNull() ?: return null
    return DateState(day = day, month = month, year = year)
}
