package org.bkkz.lumaapp.presentation.main.setting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.setting.state.SettingsEvent
import org.bkkz.lumaapp.presentation.main.setting.state.SettingsState
import org.bkkz.lumaapp.util.enums.ServiceState

class SettingsViewModel(private val repository: Repository) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when(event) {
            is SettingsEvent.OnLoadServiceStatus -> {
                _state.update { it.copy(serviceState = ServiceState.LOADING) }
                checkGoogleCalendarConnection()
            }
        }
    }

    fun checkGoogleCalendarConnection() {
        viewModelScope.launch {
            val response = repository.getUserData()
            when(response){
                is ApiResult.Success -> {
                    val userInfo = response.data
                    if(userInfo != null){
                        Log.d("SettingsViewModel", "UserInfo: $userInfo")
                        _state.update { it.copy(
                            isConnectedToCalendar = !userInfo.googleRefreshToken.isNullOrEmpty(),
                            isLoginViaGoogle = userInfo.provider == 1,
                            googleCalendarEmail = userInfo.googleCalendarEmail,
                            serviceState = ServiceState.SUCCESS
                        ) }
                    }

                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isConnectedToCalendar = false, serviceState = ServiceState.FAILED) }
                }
            }
        }
    }

    fun revokeCalendarConnection(){
        viewModelScope.launch {
            val response = repository.revokeGoogleCalendarAuth()
            when(response){
                is ApiResult.Success -> {
                    _state.update { it.copy(
                        googleCalendarEmail = null,
                        isConnectedToCalendar = false,
                        serviceState = ServiceState.SUCCESS)
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(serviceState = ServiceState.FAILED) }
                }
            }
        }
    }

    fun saveCalendarRefreshToken(authCode : String, email: String){
        viewModelScope.launch {
            val response = repository.authToCalendarService(authCode, email)
            when(response){
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(isConnectedToCalendar = true, serviceState = ServiceState.SUCCESS)
                    }
                    onEvent(SettingsEvent.OnLoadServiceStatus)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isConnectedToCalendar = false, serviceState = ServiceState.FAILED) }
                }
            }
        }
    }
}