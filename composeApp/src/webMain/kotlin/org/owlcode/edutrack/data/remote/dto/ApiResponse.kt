package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable

/** Wrapper genérico para todas las respuestas del backend: { success, data, message } */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

@Serializable
data class AuthData(
    val token: String,
    val tokenType: String = "Bearer",
    val user: UserDto
)

