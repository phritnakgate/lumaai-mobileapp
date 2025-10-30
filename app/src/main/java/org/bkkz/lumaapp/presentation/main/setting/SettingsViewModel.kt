package org.bkkz.lumaapp.presentation.main.setting

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
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
import java.util.Locale
import androidx.core.content.edit
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.util.dialog.OneActionDialog

class SettingsViewModel(private val repository: Repository) : ViewModel() {

    private val _state: MutableStateFlow<SettingsState> = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when(event) {
            is SettingsEvent.OnLoadServiceStatus -> {
                _state.update { it.copy(serviceState = ServiceState.LOADING) }
                loadServiceStatus()
            }
        }
    }

    fun loadServiceStatus() {
        val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: Locale.getDefault().language

        viewModelScope.launch {
            val response = repository.getUserData()
            when(response){
                is ApiResult.Success -> {
                    val userInfo = response.data
                    if(userInfo != null){
                        Log.d("SettingsViewModel", "UserInfo: $userInfo")
                        _state.update { it.copy(
                            currentLanguage = currentLang,
                            isConnectedToCalendar = !userInfo.googleRefreshToken.isNullOrEmpty(),
                            isLoginViaGoogle = userInfo.provider == 1,
                            googleCalendarEmail = userInfo.googleCalendarEmail,
                            serviceState = ServiceState.SUCCESS
                        ) }
                    }

                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                        isConnectedToCalendar = false,
                        serviceState = ServiceState.FAILED,
                        serviceMessage = response.exception.message
                        )
                    }
                }
            }
        }
    }

    fun revokeCalendarConnection(context: Context){
        viewModelScope.launch {
            val response = repository.revokeGoogleCalendarAuth()
            val sharedPref = context.getSharedPreferences("userSession", MODE_PRIVATE)
            when(response){
                is ApiResult.Success -> {
                    sharedPref.edit {
                        remove("googleCalendarEmail")
                        apply()
                    }
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

    fun saveCalendarRefreshToken(context: Context, authCode : String, email: String){
        viewModelScope.launch {
            val response = repository.authToCalendarService(authCode, email)
            when(response){
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(isConnectedToCalendar = true, serviceState = ServiceState.SUCCESS)
                    }
                    onEvent(SettingsEvent.OnLoadServiceStatus)
                    OneActionDialog(context).show(
                        drawable = R.drawable.ic_dialog_success,
                        title = context.getString(R.string.login_ggc_completed_dialog_title),
                        message = "",
                    )
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isConnectedToCalendar = false, serviceState = ServiceState.FAILED) }
                    OneActionDialog(context).show(
                        drawable = R.drawable.ic_dialog_no,
                        title = context.getString(R.string.login_ggc_failed_dialog_title),
                        message = context.getString(R.string.login_ggc_failed_dialog_desc),
                    )
                }
            }
        }
    }

    fun clearCredentials(context: Context){
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        }
    }

    fun changeAppLanguage(languageCode: String){
        viewModelScope.launch {
            _state.update { it.copy(currentLanguage = languageCode) }
        }
    }
}