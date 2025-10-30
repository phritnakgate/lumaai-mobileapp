package org.bkkz.lumaapp.presentation.main.chat_history.state

import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.util.enums.ServiceState

data class ChatHistoryState(
    val serviceState: ServiceState = ServiceState.IDLE,
    val serviceMessage : String? = null,
    val chatHistoryTask : List<ChatHistory> = emptyList(),
    val chatHistorySearch : List<ChatHistory> = emptyList(),
    val chatHistoryPlan : List<ChatHistory> = emptyList(),
    val chatHistoryGenForm : List<ChatHistory> = emptyList(),
    val queriedDate : String? = null,
    val queriedKeyword : String? = null,
    val currentChatHistoryPage : String = "Task" // Task, Search, Plan, GenForm
)
