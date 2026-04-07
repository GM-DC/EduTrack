package org.owlcode.edutrack.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole
)

enum class UserRole { STUDENT, TEACHER, ADMIN }

