package org.owlcode.edutrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EventoPersonal(
    val id: String,
    val titulo: String,
    val descripcion: String = "",
    val fecha: String,                   // "YYYY-MM-DD"
    val horaInicio: String? = null,      // "HH:mm"
    val horaFin: String? = null          // "HH:mm"
)

