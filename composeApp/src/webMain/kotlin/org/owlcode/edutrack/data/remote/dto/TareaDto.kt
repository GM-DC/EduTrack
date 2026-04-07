package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.model.TaskPriority
import org.owlcode.edutrack.domain.model.TaskStatus

@Serializable
data class TareaDto(
    val id: String,
    val courseId: String,
    val titulo: String,
    val descripcion: String = "",
    val prioridad: String = TaskPriority.MEDIUM.name,
    val startDate: String? = null,
    val dueDate: String,
    val dueTime: String? = null,
    val endDate: String? = null,
    val estado: String = TaskStatus.PENDING.name
) {
    fun toDomain(): Tarea = Tarea(
        id          = id,
        courseId    = courseId,
        titulo      = titulo,
        descripcion = descripcion,
        prioridad   = TaskPriority.valueOf(prioridad),
        startDate   = startDate,
        dueDate     = dueDate,
        dueTime     = dueTime,
        endDate     = endDate,
        estado      = TaskStatus.valueOf(estado)
    )
}

fun Tarea.toDto(): TareaDto = TareaDto(
    id          = id,
    courseId    = courseId,
    titulo      = titulo,
    descripcion = descripcion,
    prioridad   = prioridad.name,
    startDate   = startDate,
    dueDate     = dueDate,
    dueTime     = dueTime,
    endDate     = endDate,
    estado      = estado.name
)


