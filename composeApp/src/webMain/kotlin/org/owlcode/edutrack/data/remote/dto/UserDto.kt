package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String = "$firstName $lastName",
    val isActive: Boolean = true
)
