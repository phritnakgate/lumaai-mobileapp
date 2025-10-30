package org.bkkz.lumaapp.presentation.auth.forget

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.auth.forget.state.ForgetPasswordEvent
import org.bkkz.lumaapp.presentation.auth.forget.state.ForgetPasswordState
import org.bkkz.lumaapp.util.enums.ServiceState

class ForgetPasswordViewModel(private val repository: Repository) : ViewModel() {

    private val _state: MutableStateFlow<ForgetPasswordState> = MutableStateFlow(ForgetPasswordState())
    val state: StateFlow<ForgetPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgetPasswordEvent){
        when(event){
            is ForgetPasswordEvent.OnEmailChange -> {
                val isValid = event.email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(
                    event.email
                ).matches()
                _state.update {
                    it.copy(
                        email = event.email,
                        isEmailValid = isValid
                    )
                }
            }
            is ForgetPasswordEvent.SubmitRequest -> {
                _state.update {
                    it.copy(
                        serviceState = ServiceState.LOADING
                    )
                }
                if(state.value.isEmailValid == false || state.value.email.isNullOrEmpty()){
                    _state.update {
                        it.copy(
                            serviceState = ServiceState.FAILED
                        )
                    }
                    return
                }else{
                    resetPassword(state.value.email!!)
                }

            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
           val result = repository.resetPassword(email)
            when(result){
                is ApiResult.Success<*> -> {
                    _state.update {
                        it.copy(
                            serviceState = ServiceState.SUCCESS
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            serviceState = ServiceState.FAILED,
                            serviceMessage = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun setIdleState() {
        _state.update {
            it.copy(
                serviceState = ServiceState.IDLE
            )
        }
    }

}