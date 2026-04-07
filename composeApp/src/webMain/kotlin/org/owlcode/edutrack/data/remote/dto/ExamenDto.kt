package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.ExamStatus

@Serializable
data class ExamenDto(
    val id: String,
    val courseId: String,
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
        id         = id,
        courseId   = courseId,
        titulo     = titulo,
        tema       = tema,
        puntaje    = puntaje,
        fecha      = fecha,
        horaInicio = horaInicio,
        horaFin    = horaFin,
        duracion   = duracion,
        estado     = ExamStatus.valueOf(estado)
    )
}

fun Examen.toDto(): ExamenDto = ExamenDto(
    id         = id,
    courseId   = courseId,
    titulo     = titulo,
    tema       = tema,
    puntaje    = puntaje,
    fecha      = fecha,
    horaInicio = horaInicio,
    horaFin    = horaFin,
    duracion   = duracion,
    estado     = estado.name
)

