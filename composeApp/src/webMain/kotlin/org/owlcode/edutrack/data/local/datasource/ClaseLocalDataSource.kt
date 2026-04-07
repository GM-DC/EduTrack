package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.ClaseDto

class ClaseLocalDataSource(private val driver: StorageDriver) {

    private companion object { const val STORE = "clases" }

    suspend fun save(clase: ClaseDto) =
        driver.put(STORE, clase.id, Json.encodeToString(clase))

    suspend fun get(id: String): ClaseDto? =
        driver.get(STORE, id)?.let { runCatching { Json.decodeFromString<ClaseDto>(it) }.getOrNull() }

    suspend fun getAll(): List<ClaseDto> =
        driver.getAll(STORE).mapNotNull { runCatching { Json.decodeFromString<ClaseDto>(it) }.getOrNull() }

    suspend fun delete(id: String) = driver.delete(STORE, id)

    suspend fun clear() = driver.clear(STORE)
}

