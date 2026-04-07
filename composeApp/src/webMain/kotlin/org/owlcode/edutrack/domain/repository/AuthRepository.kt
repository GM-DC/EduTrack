package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun logout(): AppResult<Unit>
    suspend fun currentUser(): AppResult<User?>
    suspend fun isAuthenticated(): Boolean
}

