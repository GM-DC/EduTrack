package org.owlcode.edutrack.sync

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
    /** El servidor rechazó el token (HTTP 401): hay que redirigir al login. */
    data object SessionExpired : SyncState()
}

