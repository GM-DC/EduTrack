package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.EventoPersonal
import org.owlcode.edutrack.domain.repository.PersonalRepository

class MockPersonalRepository : PersonalRepository {

    private val eventos = mutableListOf(
        EventoPersonal("p1", titulo = "Reunión de Estudio",    descripcion = "Repasar temas del parcial", fecha = "2026-04-28", horaInicio = "16:00", horaFin = "18:00"),
        EventoPersonal("p2", titulo = "Repaso Final",          descripcion = "Sala de estudio virtual",   fecha = "2026-05-01", horaInicio = "19:00", horaFin = "21:00"),
        EventoPersonal("p3", titulo = "Asesoría con tutor",    descripcion = "Cubículo 204",              fecha = "2026-05-06", horaInicio = "11:00", horaFin = "12:00"),
        EventoPersonal("p4", titulo = "Estudio en biblioteca", descripcion = "",                          fecha = "2026-05-09", horaInicio = "09:00", horaFin = "13:00"),
    )

    private var nextId = 5

    override suspend fun getAllPersonales(): AppResult<List<EventoPersonal>> =
        AppResult.Success(eventos.toList())

    override suspend fun getPersonal(id: String): AppResult<EventoPersonal?> =
        AppResult.Success(eventos.find { it.id == id })

    override suspend fun addPersonal(evento: EventoPersonal): AppResult<EventoPersonal> {
        val nuevo = evento.copy(id = "p${nextId++}")
        eventos.add(nuevo)
        return AppResult.Success(nuevo)
    }

    override suspend fun updatePersonal(evento: EventoPersonal): AppResult<EventoPersonal> {
        val idx = eventos.indexOfFirst { it.id == evento.id }
        return if (idx >= 0) {
            eventos[idx] = evento
            AppResult.Success(evento)
        } else {
            AppResult.Error(AppError.Network("Evento personal no encontrado: ${evento.id}"))
        }
    }

    override suspend fun deletePersonal(id: String): AppResult<Unit> {
        eventos.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}

