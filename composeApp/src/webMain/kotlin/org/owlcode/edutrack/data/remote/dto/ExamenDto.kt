package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.ExamStatus

@Serializable
data class ExamenDto(
    val id: Long = 0L,
    val courseId: Long,
    val titulo: String,
    val tema: String = "",
    val puntaje: Float? = null,
    val fecha: String,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val duracion: Int? = null,
    val estado: String = ExamStatus.PENDING.name
) {
    fun toDomain(): Examen = Examen(
        id         = id.toString(),
        courseId   = courseId.toString(),
        titulo     = titulo,
        tema       = tema,
        puntaje    = puntaje,
        fecha      = fecha,
        horaInicio = horaInicio,
        horaFin    = horaFin,
        duracion   = duracion,
        estado     = runCatching { ExamStatus.valueOf(estado) }.getOrDefault(ExamStatus.PENDING)
    )
}

fun Examen.toDto(): ExamenDto = ExamenDto(
    id         = id.toLongOrNull() ?: 0L,
    courseId   = courseId.toLongOrNull() ?: 0L,
    titulo     = titulo,
    tema       = tema,
    puntaje    = puntaje,
    fecha      = fecha,
    horaInicio = horaInicio,
    horaFin    = horaFin,
    duracion   = duracion,
    estado     = estado.name
)

