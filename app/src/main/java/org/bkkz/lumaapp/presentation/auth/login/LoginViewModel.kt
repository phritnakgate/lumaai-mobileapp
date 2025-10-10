package org.bkkz.lumaapp.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.auth.login.state.LoginEvent

class LoginViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow<LoginEvent>(LoginEvent.Idle)
    val state: StateFlow<LoginEvent> = _state.asStateFlow()

     fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginEvent.Loading
            when(val result = repository.loginWithEmail(email, password)){
                is ApiResult.Success -> {
                    _state.value = LoginEvent.Success(email)

                }

                is ApiResult.Error -> _state.value = LoginEvent.Error(result.exception.message ?: "Login failed with unknown error :(")
            }
        }
    }

    fun loginWithGoogle(authenticatedEmail : String, idToken: String) {
        viewModelScope.launch {
            _state.value = LoginEvent.Loading
            when (val result = repository.loginWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    _state.value = LoginEvent.Success(authenticatedEmail)
                }
                is ApiResult.Error -> _state.value = LoginEvent.Error(result.exception.message ?: "Login failed with unknown error :(")
            }
        }
    }

    fun saveCalendarRefreshToken(authCode : String, email: String){
        viewModelScope.launch {
            repository.authToCalendarService(authCode, email)
        }
    }
}