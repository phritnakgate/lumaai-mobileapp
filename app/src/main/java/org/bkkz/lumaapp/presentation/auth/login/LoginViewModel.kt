package org.bkkz.lumaapp.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.remote.Repository
import org.bkkz.lumaapp.data.remote.Result
import org.bkkz.lumaapp.presentation.auth.login.state.LoginState

class LoginViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

     fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            when(val result = repository.loginWithEmail(email, password)){
                is Result.Success -> _state.value = LoginState.Success("Login Success")
                is Result.Error -> _state.value = LoginState.Error(result.exception.message ?: "Login failed with unknown error :(")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            when (val result = repository.loginWithGoogle(idToken)) {
                is Result.Success -> _state.value = LoginState.Success("Login successful!")
                is Result.Error -> _state.value = LoginState.Error(result.exception.message ?: "Login failed with unknown error :(")
            }
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            when (val result = repository.refreshToken()) {
                is Result.Success -> {
                    if (result.data) {
                        _state.value = LoginState.Success("Session restored")
                    } else {
                        _state.value = LoginState.Idle
                    }
                }
                is Result.Error -> {
                    _state.value = LoginState.Idle
                }
            }
        }
    }
}