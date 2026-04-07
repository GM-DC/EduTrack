package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.CalendarEventDto

class CalendarLocalDataSource(private val driver: StorageDriver) {

    private companion object {
        const val STORE = "calendar"
    }

    suspend fun saveEvent(event: CalendarEventDto) =
        driver.put(STORE, event.id, Json.encodeToString(event))

    suspend fun getEvent(id: String): CalendarEventDto? =
        driver.get(STORE, id)?.let { Json.decodeFromString(it) }

    suspend fun getAllEvents(): List<CalendarEventDto> =
        driver.getAll(STORE).mapNotNull {
            runCatching { Json.decodeFromString<CalendarEventDto>(it) }.getOrNull()
        }

    suspend fun deleteEvent(id: String) = driver.delete(STORE, id)

    suspend fun saveAllEvents(events: List<CalendarEventDto>) {
        driver.clear(STORE)
        events.forEach { saveEvent(it) }
    }
}

