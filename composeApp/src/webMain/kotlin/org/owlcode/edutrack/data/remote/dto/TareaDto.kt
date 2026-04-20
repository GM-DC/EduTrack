package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.model.TaskPriority
import org.owlcode.edutrack.domain.model.TaskStatus

@Serializable
data class TareaDto(
    val id: Long = 0L,
    val courseId: Long,
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
        id          = id.toString(),
        courseId    = courseId.toString(),
        titulo      = titulo,
        descripcion = descripcion,
        prioridad   = runCatching { TaskPriority.valueOf(prioridad) }.getOrDefault(TaskPriority.MEDIUM),
        startDate   = startDate,
        dueDate     = dueDate,
        dueTime     = dueTime,
        endDate     = endDate,
        estado      = runCatching { TaskStatus.valueOf(estado) }.getOrDefault(TaskStatus.PENDING)
    )
}

fun Tarea.toDto(): TareaDto = TareaDto(
    id          = id.toLongOrNull() ?: 0L,
    courseId    = courseId.toLongOrNull() ?: 0L,
    titulo      = titulo,
    descripcion = descripcion,
    prioridad   = prioridad.name,
    startDate   = startDate,
    dueDate     = dueDate,
    dueTime     = dueTime,
    endDate     = endDate,
    estado      = estado.name
)


