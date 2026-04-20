package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.mapper.toLocalDomain
import org.owlcode.edutrack.data.remote.api.AuthApiService
import org.owlcode.edutrack.data.remote.mapper.toDomain
import org.owlcode.edutrack.domain.model.User
import org.owlcode.edutrack.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthApiService,
    private val local: AuthLocalDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<User> =
        safeApiCall {
            val authData = remote.login(email, password)
            local.saveToken(authData.token)
            local.saveUser(authData.user)
            authData.user.toDomain()
        }

    override suspend fun logout(): AppResult<Unit> = safeApiCall {
        val token = local.getToken() ?: return@safeApiCall
        remote.logout(token)
        local.clearAll()
    }

    override suspend fun currentUser(): AppResult<User?> = safeApiCall {
        local.getUser()?.toLocalDomain()
    }

    override suspend fun isAuthenticated(): Boolean =
        local.getToken() != null
}
