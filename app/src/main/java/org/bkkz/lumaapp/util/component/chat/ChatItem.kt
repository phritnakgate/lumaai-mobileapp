package org.bkkz.lumaapp.util.component.chat

sealed class ChatItem {
    data class ChatUser(val message : String) : ChatItem()
    data class ChatResponse(val message : String) : ChatItem()
    data class ChatGetTask(val taskName : String, val taskDesc: String, val taskDateTime: String) : ChatItem()
    data class ChatAddTask(val taskName : String, val taskDesc: String, val taskDateTime: String, val actionCompleted: Boolean) : ChatItem()
    data class ChatEditTask(val id: String, val taskName : String, val taskDesc: String, val taskDateTime: String, val actionCompleted: Boolean) : ChatItem()
    data class ChatDeleteTask(val id: String, val taskName : String, val taskDesc: String, val taskDateTime: String, val actionCompleted: Boolean) : ChatItem()
    data class ChatWebSearch(val url: String) : ChatItem()
}