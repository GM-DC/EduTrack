package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.UserDto

class AuthLocalDataSource(private val driver: StorageDriver) {

    private companion object {
        const val STORE = "auth"
        const val KEY_TOKEN = "token"
        const val KEY_USER = "user"
    }

    suspend fun saveToken(token: String) = driver.put(STORE, KEY_TOKEN, token)
    suspend fun getToken(): String? = driver.get(STORE, KEY_TOKEN)
    suspend fun clearToken() = driver.delete(STORE, KEY_TOKEN)

    suspend fun saveUser(user: UserDto) =
        driver.put(STORE, KEY_USER, Json.encodeToString(user))

    suspend fun getUser(): UserDto? =
        driver.get(STORE, KEY_USER)?.let { Json.decodeFromString(it) }

    suspend fun clearAll() = driver.clear(STORE)
}

