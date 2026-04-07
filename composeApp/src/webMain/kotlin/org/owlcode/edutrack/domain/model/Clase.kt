package org.owlcode.edutrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Clase(
    val id: String,
    val courseId: String,
    val titulo: String = "",
    val modalidad: ClassMode = ClassMode.PRESENTIAL,
    val docente: String = "",
    val aula: String = "",
    val enlace: String = "",
    val notas: String = "",
    val startDate: String,               // "YYYY-MM-DD"
    val endDate: String = startDate,     // "YYYY-MM-DD" (igual a startDate si es única)
    val startTime: String? = null,       // "HH:mm" (null = A tu ritmo / all-day)
    val endTime: String? = null,         // "HH:mm"
    val recurrenceRule: RecurrenceRule? = null,
    val daysOfWeek: Set<Int> = emptySet(), // 0=Lu … 6=Do (activo cuando recurrencia WEEKLY)
    val isCancelled: Boolean = false,
    val parentSeriesId: String? = null   // id de la clase-padre al editar una ocurrencia
)

