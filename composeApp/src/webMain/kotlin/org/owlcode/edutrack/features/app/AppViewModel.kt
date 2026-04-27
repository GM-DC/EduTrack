package org.owlcode.edutrack.features.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.owlcode.edutrack.domain.repository.AuthRepository
import org.owlcode.edutrack.sync.SyncManager
import org.owlcode.edutrack.sync.SyncState

class AppViewModel(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    /** true una vez que se ha comprobado el estado de autenticación inicial */
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    val syncState: StateFlow<SyncState> = syncManager.syncState

    init {
        viewModelScope.launch {
            val authenticated = authRepository.isAuthenticated()
            _isAuthenticated.value = authenticated

            // ⚠️ CRÍTICO: marcar como inicializado ANTES del sync.
            // El sync es una operación de red que puede tardar o fallar;
            // no debe bloquear el montaje de la UI.
            _isInitialized.value = true

            if (authenticated) {
                syncManager.syncAll()
                // Si el servidor rechazó el token (401), limpiar sesión y
                // redirigir al login sin que el usuario tenga que limpiar caché.
                if (syncState.value == SyncState.SessionExpired) {
                    authRepository.logout()
                    _isAuthenticated.value = false
                }
            }
        }
    }

    fun onLoggedIn() {
        _isAuthenticated.value = true
        viewModelScope.launch { syncManager.syncAll() }
    }

    fun onLoggedOut() {
        _isAuthenticated.value = false
    }
}

