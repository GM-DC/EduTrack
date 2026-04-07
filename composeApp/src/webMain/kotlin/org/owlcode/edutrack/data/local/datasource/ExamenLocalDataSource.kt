package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.ExamenDto

class ExamenLocalDataSource(private val driver: StorageDriver) {

    private companion object { const val STORE = "examenes" }

    suspend fun save(examen: ExamenDto) =
        driver.put(STORE, examen.id, Json.encodeToString(examen))

    suspend fun get(id: String): ExamenDto? =
        driver.get(STORE, id)?.let { runCatching { Json.decodeFromString<ExamenDto>(it) }.getOrNull() }

    suspend fun getAll(): List<ExamenDto> =
        driver.getAll(STORE).mapNotNull { runCatching { Json.decodeFromString<ExamenDto>(it) }.getOrNull() }

    suspend fun delete(id: String) = driver.delete(STORE, id)

    suspend fun clear() = driver.clear(STORE)
}

