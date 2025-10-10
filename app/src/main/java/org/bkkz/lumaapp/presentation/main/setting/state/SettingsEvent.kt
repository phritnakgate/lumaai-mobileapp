package org.bkkz.lumaapp.presentation.main.setting.state

sealed class SettingsEvent {
    data object OnLoadServiceStatus : SettingsEvent()
}