package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.ClassMode
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.RecurrenceRule

@Serializable
data class ClaseDto(
    val id: Long = 0L,
    val courseId: Long,
    val titulo: String = "",
    val modalidad: String = ClassMode.PRESENTIAL.name,
    val docente: String = "",
    val aula: String? = null,
    val enlace: String? = null,
    val notas: String = "",
    val startDate: String,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val daysOfWeek: Set<Int> = emptySet(),
    val isCancelled: Boolean = false,
    val parentSeriesId: Long? = null
) {
    fun toDomain(): Clase = Clase(
        id             = id.toString(),
        courseId       = courseId.toString(),
        titulo         = titulo,
        modalidad      = runCatching { ClassMode.valueOf(modalidad) }.getOrDefault(ClassMode.PRESENTIAL),
        docente        = docente,
        aula           = aula ?: "",
        enlace         = enlace ?: "",
        notas          = notas,
        startDate      = startDate,
        endDate        = endDate ?: startDate,
        startTime      = startTime,
        endTime        = endTime,
        recurrenceRule = recurrenceRule,
        daysOfWeek     = daysOfWeek,
        isCancelled    = isCancelled,
        parentSeriesId = parentSeriesId?.toString()
    )
}

fun Clase.toDto(): ClaseDto = ClaseDto(
    id             = id.toLongOrNull() ?: 0L,
    courseId       = courseId.toLongOrNull() ?: 0L,
    titulo         = titulo,
    modalidad      = modalidad.name,
    docente        = docente,
    aula           = aula.ifBlank { null },
    enlace         = enlace.ifBlank { null },
    notas          = notas,
    startDate      = startDate,
    endDate        = endDate.ifBlank { null },
    startTime      = startTime,
    endTime        = endTime,
    recurrenceRule = recurrenceRule,
    daysOfWeek     = daysOfWeek,
    isCancelled    = isCancelled,
    parentSeriesId = parentSeriesId?.toLongOrNull()
)

