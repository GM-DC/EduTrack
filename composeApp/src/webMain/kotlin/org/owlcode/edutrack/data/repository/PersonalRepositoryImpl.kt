package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.PersonalLocalDataSource
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.EventoPersonal
import org.owlcode.edutrack.domain.repository.PersonalRepository

class PersonalRepositoryImpl(
    private val localDataSource: PersonalLocalDataSource
) : PersonalRepository {

    override suspend fun getAllPersonales(): AppResult<List<EventoPersonal>> = safeApiCall {
        localDataSource.getAll().map { it.toDomain() }
    }

    override suspend fun getPersonal(id: String): AppResult<EventoPersonal?> = safeApiCall {
        localDataSource.get(id)?.toDomain()
    }

    override suspend fun addPersonal(evento: EventoPersonal): AppResult<EventoPersonal> = safeApiCall {
        localDataSource.save(evento.toDto())
        evento
    }

    override suspend fun updatePersonal(evento: EventoPersonal): AppResult<EventoPersonal> = safeApiCall {
        localDataSource.save(evento.toDto())
        evento
    }

    override suspend fun deletePersonal(id: String): AppResult<Unit> = safeApiCall {
        localDataSource.delete(id)
    }
}

