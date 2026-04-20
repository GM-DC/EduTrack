package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.EventoPersonalDto

class EventoPersonalApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getEventos(token: String): List<EventoPersonalDto> {
        val response: ApiResponse<List<EventoPersonalDto>> =
            client.get("$baseUrl/api/eventos-personales") {
                bearerAuth(token)
            }.body()
        return response.data ?: emptyList()
    }

    suspend fun getEvento(token: String, id: Long): EventoPersonalDto {
        val response: ApiResponse<EventoPersonalDto> =
            client.get("$baseUrl/api/eventos-personales/$id") {
                bearerAuth(token)
            }.body()
        return response.data ?: error("Evento no encontrado")
    }

    suspend fun addEvento(token: String, evento: EventoPersonalDto): EventoPersonalDto {
        val response: ApiResponse<EventoPersonalDto> =
            client.post("$baseUrl/api/eventos-personales") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(evento)
            }.body()
        return response.data ?: error("Error al crear evento")
    }

    suspend fun updateEvento(token: String, evento: EventoPersonalDto): EventoPersonalDto {
        val response: ApiResponse<EventoPersonalDto> =
            client.put("$baseUrl/api/eventos-personales/${evento.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(evento)
            }.body()
        return response.data ?: error("Error al actualizar evento")
    }

    suspend fun deleteEvento(token: String, id: Long) {
        client.delete("$baseUrl/api/eventos-personales/$id") {
            bearerAuth(token)
        }
    }
}

