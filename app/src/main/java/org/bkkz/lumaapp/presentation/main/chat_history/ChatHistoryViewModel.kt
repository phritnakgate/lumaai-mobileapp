package org.bkkz.lumaapp.presentation.main.chat_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.chat_history.state.ChatHistoryEvent
import org.bkkz.lumaapp.presentation.main.chat_history.state.ChatHistoryState
import org.bkkz.lumaapp.util.enums.ServiceState

class ChatHistoryViewModel(private val repository: Repository) : ViewModel(){
    private val _state : MutableStateFlow<ChatHistoryState> = MutableStateFlow(ChatHistoryState())
    val state : StateFlow<ChatHistoryState> = _state.asStateFlow()

    fun onEvent(event: ChatHistoryEvent){
        when(event){
            is ChatHistoryEvent.OnLoadFirstTimeChatHistory -> {
                _state.update { it.copy(
                    serviceState = ServiceState.LOADING
                ) }
                getChatHistory()
            }
            is ChatHistoryEvent.SelectChatHistoryType -> {
                _state.update { it.copy(
                    currentChatHistoryPage = event.type
                ) }
            }
            is ChatHistoryEvent.OnQueryByDate -> {
                _state.update { it.copy(
                    queriedDate = event.date,
                    serviceState = ServiceState.LOADING
                ) }
                getChatHistory()
            }
            is ChatHistoryEvent.OnQueryByKeyword -> {
                _state.update { it.copy(
                    queriedKeyword = event.keyword,
                    serviceState = ServiceState.LOADING
                ) }
                getChatHistory()
            }
        }
    }

    private fun getChatHistory(){
        viewModelScope.launch {
            val taskChatHistory = repository.getChatLogs(intent = "Task", date = state.value.queriedDate, keyword=state.value.queriedKeyword)
            val searchChatHistory = repository.getChatLogs(intent = "Search", date = state.value.queriedDate, keyword=state.value.queriedKeyword)
            val planChatHistory = repository.getChatLogs(intent = "Plan", date = state.value.queriedDate, keyword=state.value.queriedKeyword)
            when(taskChatHistory){
                is ApiResult.Success -> {
                    _state.update { it.copy(
                        chatHistoryTask = taskChatHistory.data ?: emptyList()
                    ) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                        serviceState = ServiceState.FAILED
                    ) }
                    return@launch
                }
            }
            when(searchChatHistory){
                is ApiResult.Success -> {
                    _state.update { it.copy(
                        chatHistorySearch = searchChatHistory.data ?: emptyList()
                    ) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                        serviceState = ServiceState.FAILED
                    ) }
                    return@launch
                }
            }
            when(planChatHistory){
                is ApiResult.Success -> {
                    _state.update { it.copy(
                        chatHistoryPlan = planChatHistory.data ?: emptyList()
                    ) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(
                        serviceState = ServiceState.FAILED
                    ) }
                    return@launch
                }
            }
            _state.update { it.copy(
                serviceState = ServiceState.SUCCESS
            ) }
        }
    }
}