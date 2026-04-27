package org.owlcode.edutrack.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.repository.CalendarRepository

class SyncManager(
    private val calendarRepository: CalendarRepository
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun syncAll() {
        _syncState.value = SyncState.Syncing
        _syncState.value = when (val result = calendarRepository.syncEvents()) {
            is AppResult.Success -> SyncState.Success
            is AppResult.Error   -> when (result.error) {
                is AppError.Auth -> SyncState.SessionExpired
                else             -> SyncState.Error(result.error.message)
            }
        }
    }
}

