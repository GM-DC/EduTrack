package org.owlcode.edutrack.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.repository.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            _state.value = when (val result = authRepository.login(email, password)) {
                is AppResult.Success -> LoginState.Success(result.data)
                is AppResult.Error   -> LoginState.Error(result.error.message)
            }
        }
    }

    fun reset() {
        _state.value = LoginState.Idle
    }
}

