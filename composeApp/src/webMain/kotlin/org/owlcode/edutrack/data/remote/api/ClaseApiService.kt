package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.ClaseDto

class ClaseApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getClasesByCourse(token: String, courseId: Long): List<ClaseDto> {
        println("[ClaseApiService] Petición GET a /api/courses/$courseId/clases")
        val response: ApiResponse<List<ClaseDto>> =
            client.get("$baseUrl/api/courses/$courseId/clases") {
                bearerAuth(token)
            }.body()
        println("[ClaseApiService] Respuesta de /api/courses/$courseId/clases: ${response.data}")
        return response.data ?: emptyList()
    }

    suspend fun getClase(token: String, id: Long): ClaseDto {
        val response: ApiResponse<ClaseDto> =
            client.get("$baseUrl/api/clases/$id") {
                bearerAuth(token)
            }.body()
        return response.data ?: error("Clase no encontrada")
    }

    suspend fun addClase(token: String, clase: ClaseDto): ClaseDto {
        val response: ApiResponse<ClaseDto> =
            client.post("$baseUrl/api/clases") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(clase)
            }.body()
        return response.data ?: error("Error al crear clase")
    }

    suspend fun updateClase(token: String, clase: ClaseDto): ClaseDto {
        val response: ApiResponse<ClaseDto> =
            client.put("$baseUrl/api/clases/${clase.id}") {
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                setBody(clase)
            }.body()
        return response.data ?: error("Error al actualizar clase")
    }

    suspend fun deleteClase(token: String, id: Long) {
        client.delete("$baseUrl/api/clases/$id") {
            bearerAuth(token)
        }
    }
}
