 package org.owlcode.edutrack.domain.model

/**
 * Modelo visual unificado para el calendario.
 * Generado por el motor de recurrencias a partir de [Clase], [Tarea], [Examen] o [EventoPersonal].
 * Es el único tipo que consume la UI del calendario.
 */
data class CalendarItem(
    val id: String,                        // único por ocurrencia (sourceId_fecha)
    val sourceId: String,                  // id de la entidad original
    val type: EventType,                   // CLASS / TASK / EXAM / PERSONAL
    val title: String,
    val date: String,                      // "YYYY-MM-DD"
    val startTime: String? = null,         // "HH:mm"
    val endTime: String? = null,           // "HH:mm"
    val isAllDay: Boolean = false,         // true → va en zona superior (sin timeline)
    val courseColor: String? = null,       // hex color del curso (null para Personal)
    val courseId: String? = null,
    val courseName: String? = null,        // nombre del curso (null para Personal)
    val status: String? = null,            // estado legible (ej. "PENDING")
    val isDeadline: Boolean = false        // true para tareas (nunca bloque principal)
)

