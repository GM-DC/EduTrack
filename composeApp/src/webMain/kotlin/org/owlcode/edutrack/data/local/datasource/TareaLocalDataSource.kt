package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.TareaDto

class TareaLocalDataSource(private val driver: StorageDriver) {

    private companion object { const val STORE = "tareas" }

    suspend fun save(tarea: TareaDto) =
        driver.put(STORE, tarea.id.toString(), Json.encodeToString(tarea))

    suspend fun get(id: String): TareaDto? =
        driver.get(STORE, id)?.let { runCatching { Json.decodeFromString<TareaDto>(it) }.getOrNull() }

    suspend fun getAll(): List<TareaDto> =
        driver.getAll(STORE).mapNotNull { runCatching { Json.decodeFromString<TareaDto>(it) }.getOrNull() }

    suspend fun delete(id: String) = driver.delete(STORE, id)

    suspend fun clear() = driver.clear(STORE)
}

