package org.bkkz.lumaapp.util.component.chat_history

sealed class ChatHistoryItem {
    data class ChatHistoryDate(val date : String) : ChatHistoryItem()
    data class ChatHistoryLists(val histories : List<String>) : ChatHistoryItem()
}