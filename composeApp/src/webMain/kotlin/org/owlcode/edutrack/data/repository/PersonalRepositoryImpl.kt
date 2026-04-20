package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.PersonalLocalDataSource
import org.owlcode.edutrack.data.remote.api.EventoPersonalApiService
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.EventoPersonal
import org.owlcode.edutrack.domain.repository.PersonalRepository

class PersonalRepositoryImpl(
    private val remote: EventoPersonalApiService,
    private val local: PersonalLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : PersonalRepository {

    override suspend fun getAllPersonales(): AppResult<List<EventoPersonal>> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dtos = remote.getEventos(token)
        dtos.forEach { local.save(it) }
        dtos.map { it.toDomain() }
    }

    override suspend fun getPersonal(id: String): AppResult<EventoPersonal?> = safeApiCall {
        local.get(id)?.toDomain()
    }

    override suspend fun addPersonal(evento: EventoPersonal): AppResult<EventoPersonal> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addEvento(token, evento.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun updatePersonal(evento: EventoPersonal): AppResult<EventoPersonal> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateEvento(token, evento.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun deletePersonal(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteEvento(token, id.toLong())
        local.delete(id)
    }
}
