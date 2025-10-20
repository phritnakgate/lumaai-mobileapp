package org.bkkz.lumaapp.presentation.main.home.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class HomeState(
    val recentChats : List<String> = emptyList(),
    val serviceState: ServiceState = ServiceState.IDLE
)
