package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.CalendarEvent

interface CalendarRepository {
    suspend fun getEvents(): AppResult<List<CalendarEvent>>
    suspend fun getEventById(id: String): AppResult<CalendarEvent?>
    suspend fun addEvent(event: CalendarEvent): AppResult<CalendarEvent>
    suspend fun updateEvent(event: CalendarEvent): AppResult<CalendarEvent>
    suspend fun deleteEvent(id: String): AppResult<Unit>
    suspend fun syncEvents(): AppResult<Unit>
}

