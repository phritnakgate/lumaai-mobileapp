package org.bkkz.lumaapp.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.chat_history.ChatHistoryActivity
import org.bkkz.lumaapp.presentation.main.home.state.HomeEvent
import org.bkkz.lumaapp.presentation.main.home.state.HomeState
import org.bkkz.lumaapp.util.enums.ServiceState

class HomeViewModel(private val repository: Repository) : ViewModel() {

    private val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnLoadRecent -> {
                _state.update { it.copy(serviceState = ServiceState.LOADING) }
                loadRecentChats()
                syncGoogleCalendar()
            }
        }
    }

    fun loadRecentChats() {
        viewModelScope.launch {
            val response = repository.getChatLogs()
            when (response) {
                is ApiResult.Success -> {
                    if (response.data == null) {
                        _state.update {
                            it.copy(
                                serviceState = ServiceState.SUCCESS,
                                recentChats = emptyList()
                            )
                        }
                    } else {
                        val recentChats = mutableListOf<String>()
                        response.data.forEach {
                            val isGenFormChat = it.intent.contains(ChatHistoryActivity.Companion.CATEGORY_GENFORM)
                            recentChats.add(if(isGenFormChat) "ทำการสร้างฟอร์มเรียบร้อย" else it.modelResponse)
                            recentChats.add(it.userText)
                        }
                        _state.update {
                            it.copy(
                                serviceState = ServiceState.SUCCESS,
                                recentChats = recentChats.toList()
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(serviceState = ServiceState.FAILED) }
                }
            }
        }
    }

    private fun syncGoogleCalendar() {
        viewModelScope.launch {
            repository.syncGoogleCalendarTasks()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}