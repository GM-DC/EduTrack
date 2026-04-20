package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.owlcode.edutrack.data.remote.dto.ApiResponse
import org.owlcode.edutrack.data.remote.dto.AuthData

@Serializable
private data class LoginRequest(val email: String, val password: String)

@Serializable
private data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

class AuthApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(email: String, password: String): AuthData {
        val response: ApiResponse<AuthData> = client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
        return response.data ?: error("Respuesta vacía del servidor")
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): AuthData {
        val response: ApiResponse<AuthData> = client.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, firstName, lastName))
        }.body()
        return response.data ?: error("Respuesta vacía del servidor")
    }

    suspend fun logout(token: String) {
        client.post("$baseUrl/api/auth/logout") {
            bearerAuth(token)
        }
    }
}

