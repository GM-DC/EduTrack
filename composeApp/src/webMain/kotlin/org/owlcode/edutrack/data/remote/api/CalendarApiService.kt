package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.CalendarEventDto

class CalendarApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getEvents(token: String): List<CalendarEventDto> =
        client.get("$baseUrl/events") {
            bearerAuth(token)
        }.body()

    suspend fun addEvent(token: String, event: CalendarEventDto): CalendarEventDto =
        client.post("$baseUrl/events") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(event)
        }.body()

    suspend fun updateEvent(token: String, event: CalendarEventDto): CalendarEventDto =
        client.put("$baseUrl/events/${event.id}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(event)
        }.body()

    suspend fun deleteEvent(token: String, id: String) {
        client.delete("$baseUrl/events/$id") {
            bearerAuth(token)
        }
    }
}

