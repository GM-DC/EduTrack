package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.owlcode.edutrack.data.remote.dto.CourseDto

class CourseApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun getCourses(token: String): List<CourseDto> =
        client.get("$baseUrl/courses") {
            bearerAuth(token)
        }.body()

    suspend fun addCourse(token: String, course: CourseDto): CourseDto =
        client.post("$baseUrl/courses") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(course)
        }.body()

    suspend fun updateCourse(token: String, course: CourseDto): CourseDto =
        client.put("$baseUrl/courses/${course.id}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(course)
        }.body()

    suspend fun deleteCourse(token: String, id: String) {
        client.delete("$baseUrl/courses/$id") {
            bearerAuth(token)
        }
    }
}

