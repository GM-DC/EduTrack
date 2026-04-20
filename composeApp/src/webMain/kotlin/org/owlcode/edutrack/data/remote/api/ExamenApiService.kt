package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.ExamenDto

class ExamenApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getExamenesByCourse(token: String, courseId: Long): List<ExamenDto> {
        val response: ApiResponse<List<ExamenDto>> =
            client.get("$baseUrl/api/courses/$courseId/examenes") {
                bearerAuth(token)
            }.body()
        return response.data ?: emptyList()
    }

    suspend fun getExamen(token: String, id: Long): ExamenDto {
        val response: ApiResponse<ExamenDto> =
            client.get("$baseUrl/api/examenes/$id") {
                bearerAuth(token)
            }.body()
        return response.data ?: error("Examen no encontrado")
    }

    suspend fun addExamen(token: String, examen: ExamenDto): ExamenDto {
        val response: ApiResponse<ExamenDto> =
            client.post("$baseUrl/api/examenes") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(examen)
            }.body()
        return response.data ?: error("Error al crear examen")
    }

    suspend fun updateExamen(token: String, examen: ExamenDto): ExamenDto {
        val response: ApiResponse<ExamenDto> =
            client.put("$baseUrl/api/examenes/${examen.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(examen)
            }.body()
        return response.data ?: error("Error al actualizar examen")
    }

    suspend fun deleteExamen(token: String, id: Long) {
        client.delete("$baseUrl/api/examenes/$id") {
            bearerAuth(token)
        }
    }
}

