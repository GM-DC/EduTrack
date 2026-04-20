package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable
import org.owlcode.edutrack.domain.model.EventoPersonal

@Serializable
data class EventoPersonalDto(
    val id: Long = 0L,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,
    val horaInicio: String? = null,
    val horaFin: String? = null
) {
    fun toDomain(): EventoPersonal = EventoPersonal(
        id          = id.toString(),
        titulo      = titulo,
        descripcion = descripcion,
        fecha       = fecha,
        horaInicio  = horaInicio,
        horaFin     = horaFin
    )
}

fun EventoPersonal.toDto(): EventoPersonalDto = EventoPersonalDto(
    id          = id.toLongOrNull() ?: 0L,
    titulo      = titulo,
    descripcion = descripcion,
    fecha       = fecha,
    horaInicio  = horaInicio,
    horaFin     = horaFin
)
