package org.bkkz.lumaapp.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.auth.register.state.RegisterState

class RegisterViewModel(private val repository: Repository) : ViewModel() {
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(email: String, password: String, name: String){
        viewModelScope.launch {
            _state.value = RegisterState.Loading
            when(val result = repository.registerWithEmail(email, password, name)){
                is ApiResult.Success -> _state.value = RegisterState.Success("Register Success")
                is ApiResult.Error -> _state.value = RegisterState.Error(result.exception.message ?: "Register failed with unknown error :(")
            }
        }
    }
}