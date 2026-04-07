package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.EventoPersonalDto

class PersonalLocalDataSource(private val driver: StorageDriver) {

    private companion object { const val STORE = "personales" }

    suspend fun save(evento: EventoPersonalDto) =
        driver.put(STORE, evento.id, Json.encodeToString(evento))

    suspend fun get(id: String): EventoPersonalDto? =
        driver.get(STORE, id)?.let { runCatching { Json.decodeFromString<EventoPersonalDto>(it) }.getOrNull() }

    suspend fun getAll(): List<EventoPersonalDto> =
        driver.getAll(STORE).mapNotNull { runCatching { Json.decodeFromString<EventoPersonalDto>(it) }.getOrNull() }

    suspend fun delete(id: String) = driver.delete(STORE, id)

    suspend fun clear() = driver.clear(STORE)
}

