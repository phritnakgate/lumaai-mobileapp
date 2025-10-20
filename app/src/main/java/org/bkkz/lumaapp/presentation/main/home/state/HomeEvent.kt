package org.bkkz.lumaapp.presentation.main.home.state

sealed class HomeEvent {
    data object OnLoadRecent : HomeEvent()
}