package org.owlcode.edutrack.features.calendar.components

/**
 * Estado interno del TimePicker (formato 12h + AM/PM).
 * [hour] va de 1 a 12; [minute] de 0 a 59.
 */
data class TimeState(
    val hour: Int   = 8,
    val minute: Int = 0,
    val isAm: Boolean = true
)

/** Formatea el estado para mostrar en la UI (ej. "08:00 AM"). */
fun TimeState.toDisplayString(): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    val period = if (isAm) "AM" else "PM"
    return "$h:$m $period"
}

/**
 * Convierte a formato 24h "HH:mm" para uso interno del sistema.
 * Devuelve cadena vacía si el estado es el predeterminado vacío.
 */
fun TimeState.to24h(): String {
    val h24 = when {
        isAm && hour == 12  -> 0
        !isAm && hour != 12 -> hour + 12
        else                -> hour
    }
    return "${h24.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

/**
 * Parsea una cadena "HH:mm" (24h) a [TimeState].
 * Devuelve null si el formato es inválido o la cadena está vacía.
 */
fun String.toTimeState(): TimeState? {
    if (isBlank()) return null
    val parts = trim().split(":")
    if (parts.size != 2) return null
    val h24 = parts[0].toIntOrNull() ?: return null
    val m   = parts[1].toIntOrNull() ?: return null
    val isAm = h24 < 12
    val h12 = when {
        h24 == 0  -> 12
        h24 > 12  -> h24 - 12
        else      -> h24
    }
    return TimeState(hour = h12, minute = m, isAm = isAm)
}

