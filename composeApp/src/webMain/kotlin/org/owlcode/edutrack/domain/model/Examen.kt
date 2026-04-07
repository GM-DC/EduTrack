package org.owlcode.edutrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Examen(
    val id: String,
    val courseId: String,
    val titulo: String,
    val tema: String = "",
    val puntaje: Float? = null,
    val fecha: String,                   // "YYYY-MM-DD"
    val horaInicio: String? = null,      // "HH:mm"
    val horaFin: String? = null,         // "HH:mm" (null = horaInicio + 1h)
    val duracion: Int? = null,           // minutos (informativo)
    val estado: ExamStatus = ExamStatus.PENDING
)

