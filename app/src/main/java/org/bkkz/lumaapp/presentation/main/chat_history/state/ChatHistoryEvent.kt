package org.bkkz.lumaapp.presentation.main.chat_history.state

sealed class ChatHistoryEvent {
    data object OnLoadFirstTimeChatHistory : ChatHistoryEvent()
    data class OnQueryByDate(val date: String?) : ChatHistoryEvent()
    data class OnQueryByKeyword(val keyword: String?) : ChatHistoryEvent()
    data class SelectChatHistoryType(val type: String) : ChatHistoryEvent()
}