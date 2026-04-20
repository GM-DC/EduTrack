package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.CourseDto

class CourseApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getCourses(token: String): List<CourseDto> {
        val response: ApiResponse<List<CourseDto>> = client.get("$baseUrl/api/courses") {
            bearerAuth(token)
        }.body()
        return response.data ?: emptyList()
    }

    suspend fun getCourseById(token: String, id: Long): CourseDto {
        val response: ApiResponse<CourseDto> = client.get("$baseUrl/api/courses/$id") {
            bearerAuth(token)
        }.body()
        return response.data ?: error("Curso no encontrado")
    }

    suspend fun addCourse(token: String, course: CourseDto): CourseDto {
        val response: ApiResponse<CourseDto> = client.post("$baseUrl/api/courses") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(course)
        }.body()
        return response.data ?: error("Error al crear curso")
    }

    suspend fun updateCourse(token: String, course: CourseDto): CourseDto {
        val response: ApiResponse<CourseDto> = client.put("$baseUrl/api/courses/${course.id}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(course)
        }.body()
        return response.data ?: error("Error al actualizar curso")
    }

    suspend fun deleteCourse(token: String, id: Long) {
        client.delete("$baseUrl/api/courses/$id") {
            bearerAuth(token)
        }
    }
}
