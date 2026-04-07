package org.owlcode.edutrack.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.owlcode.edutrack.data.remote.dto.UserDto

@Serializable
private data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val user: UserDto)

class AuthApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    suspend fun login(email: String, password: String): LoginResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun logout(token: String) {
        client.post("$baseUrl/auth/logout") {
            bearerAuth(token)
        }
    }

    suspend fun currentUser(token: String): UserDto =
        client.get("$baseUrl/auth/me") {
            bearerAuth(token)
        }.body()
}

