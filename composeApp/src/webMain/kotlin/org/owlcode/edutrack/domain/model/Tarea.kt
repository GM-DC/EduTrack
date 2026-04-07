package org.owlcode.edutrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tarea(
    val id: String,
    val courseId: String,
    val titulo: String,
    val descripcion: String = "",
    val prioridad: TaskPriority = TaskPriority.MEDIUM,
    val startDate: String? = null,       // "YYYY-MM-DD" (opcional)
    val dueDate: String,                 // "YYYY-MM-DD" (obligatorio)
    val dueTime: String? = null,         // "HH:mm" (null = todo el día)
    val endDate: String? = null,         // "YYYY-MM-DD" (para rango)
    val estado: TaskStatus = TaskStatus.PENDING
)

