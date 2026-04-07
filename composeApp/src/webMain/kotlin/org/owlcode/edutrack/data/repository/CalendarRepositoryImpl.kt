package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.CalendarLocalDataSource
import org.owlcode.edutrack.data.local.mapper.toLocalDomain
import org.owlcode.edutrack.data.remote.api.CalendarApiService
import org.owlcode.edutrack.data.remote.mapper.toDto
import org.owlcode.edutrack.data.remote.mapper.toDomain
import org.owlcode.edutrack.domain.model.CalendarEvent
import org.owlcode.edutrack.domain.repository.CalendarRepository

class CalendarRepositoryImpl(
    private val remote: CalendarApiService,
    private val local: CalendarLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : CalendarRepository {

    // offline-first: devuelve datos locales inmediatamente
    override suspend fun getEvents(): AppResult<List<CalendarEvent>> = safeApiCall {
        local.getAllEvents().map { it.toLocalDomain() }
    }

    override suspend fun getEventById(id: String): AppResult<CalendarEvent?> = safeApiCall {
        local.getEvent(id)?.toLocalDomain()
    }

    override suspend fun addEvent(event: CalendarEvent): AppResult<CalendarEvent> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addEvent(token, event.toDto())
        local.saveEvent(dto)
        dto.toDomain()
    }

    override suspend fun updateEvent(event: CalendarEvent): AppResult<CalendarEvent> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateEvent(token, event.toDto())
        local.saveEvent(dto)
        dto.toDomain()
    }

    override suspend fun deleteEvent(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteEvent(token, id)
        local.deleteEvent(id)
    }

    // sincroniza remoto → local
    override suspend fun syncEvents(): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val remoteEvents = remote.getEvents(token)
        local.saveAllEvents(remoteEvents)
    }
}

