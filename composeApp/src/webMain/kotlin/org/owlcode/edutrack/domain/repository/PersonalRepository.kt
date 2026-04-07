package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.EventoPersonal

interface PersonalRepository {
    suspend fun getAllPersonales(): AppResult<List<EventoPersonal>>
    suspend fun getPersonal(id: String): AppResult<EventoPersonal?>
    suspend fun addPersonal(evento: EventoPersonal): AppResult<EventoPersonal>
    suspend fun updatePersonal(evento: EventoPersonal): AppResult<EventoPersonal>
    suspend fun deletePersonal(id: String): AppResult<Unit>
}

