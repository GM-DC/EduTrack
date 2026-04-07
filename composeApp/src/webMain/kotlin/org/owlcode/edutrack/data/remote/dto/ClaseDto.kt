package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.ClassMode
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.RecurrenceRule

@Serializable
data class ClaseDto(
    val id: String,
    val courseId: String,
    val titulo: String = "",
    val modalidad: String = ClassMode.PRESENTIAL.name,
    val docente: String = "",
    val aula: String = "",
    val enlace: String = "",
    val notas: String = "",
    val startDate: String,
    val endDate: String = startDate,
    val startTime: String? = null,
    val endTime: String? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val daysOfWeek: Set<Int> = emptySet(),
    val isCancelled: Boolean = false,
    val parentSeriesId: String? = null
) {
    fun toDomain(): Clase = Clase(
        id             = id,
        courseId       = courseId,
        titulo         = titulo,
        modalidad      = ClassMode.valueOf(modalidad),
        docente        = docente,
        aula           = aula,
        enlace         = enlace,
        notas          = notas,
        startDate      = startDate,
        endDate        = endDate,
        startTime      = startTime,
        endTime        = endTime,
        recurrenceRule = recurrenceRule,
        daysOfWeek     = daysOfWeek,
        isCancelled    = isCancelled,
        parentSeriesId = parentSeriesId
    )
}

fun Clase.toDto(): ClaseDto = ClaseDto(
    id             = id,
    courseId       = courseId,
    titulo         = titulo,
    modalidad      = modalidad.name,
    docente        = docente,
    aula           = aula,
    enlace         = enlace,
    notas          = notas,
    startDate      = startDate,
    endDate        = endDate,
    startTime      = startTime,
    endTime        = endTime,
    recurrenceRule = recurrenceRule,
    daysOfWeek     = daysOfWeek,
    isCancelled    = isCancelled,
    parentSeriesId = parentSeriesId
)

