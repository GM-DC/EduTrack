package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.TareaDto

class TareaApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getTareasByCourse(token: String, courseId: Long): List<TareaDto> {
        val response: ApiResponse<List<TareaDto>> =
            client.get("$baseUrl/api/courses/$courseId/tareas") {
                bearerAuth(token)
            }.body()
        return response.data ?: emptyList()
    }

    suspend fun getTarea(token: String, id: Long): TareaDto {
        val response: ApiResponse<TareaDto> =
            client.get("$baseUrl/api/tareas/$id") {
                bearerAuth(token)
            }.body()
        return response.data ?: error("Tarea no encontrada")
    }

    suspend fun addTarea(token: String, tarea: TareaDto): TareaDto {
        val response: ApiResponse<TareaDto> =
            client.post("$baseUrl/api/tareas") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(tarea)
            }.body()
        return response.data ?: error("Error al crear tarea")
    }

    suspend fun updateTarea(token: String, tarea: TareaDto): TareaDto {
        val response: ApiResponse<TareaDto> =
            client.put("$baseUrl/api/tareas/${tarea.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(tarea)
            }.body()
        return response.data ?: error("Error al actualizar tarea")
    }

    suspend fun deleteTarea(token: String, id: Long) {
        client.delete("$baseUrl/api/tareas/$id") {
            bearerAuth(token)
        }
    }
}

