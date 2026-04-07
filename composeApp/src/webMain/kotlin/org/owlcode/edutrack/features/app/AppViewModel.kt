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
            _isAuthenticated.value = authRepository.isAuthenticated()
            if (_isAuthenticated.value) syncManager.syncAll()
            _isInitialized.value = true  // siempre al final, con el valor correcto ya asignado
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

