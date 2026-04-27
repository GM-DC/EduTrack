package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.User
import org.owlcode.edutrack.domain.model.UserRole
import org.owlcode.edutrack.domain.repository.AuthRepository

// TODO: reemplazar por AuthRepositoryImpl cuando el backend esté disponible
class MockAuthRepository : AuthRepository {

    private val validEmail    = "123@gmail.com"
    private val validPassword = "12345678"

    private val mockUser = User(
        id    = "mock-001",
        name  = "Usuario Demo",
        email = validEmail,
        role  = UserRole.STUDENT
    )

    private var loggedIn = false

    override suspend fun login(email: String, password: String): AppResult<User> {
        return if (email == validEmail && password == validPassword) {
            loggedIn = true
            AppResult.Success(mockUser)
        } else {
            AppResult.Error(AppError.Auth("Credenciales incorrectas"))
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        loggedIn = false
        return AppResult.Success(Unit)
    }

    override suspend fun currentUser(): AppResult<User?> =
        AppResult.Success(if (loggedIn) mockUser else null)

    override suspend fun isAuthenticated(): Boolean = loggedIn
}

