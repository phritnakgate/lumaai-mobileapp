package org.bkkz.lumaapp.presentation.main.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.local.UserChat
import org.bkkz.lumaapp.util.component.chat.ChatItem
import org.bkkz.lumaapp.util.enums.LocalChatFlag

class ChatViewModel(private val repository: Repository) : ViewModel() {

    private val _chatItems = MutableLiveData<List<ChatItem>>()
    val chatItems: LiveData<List<ChatItem>> = _chatItems

    init {
        loadChatsData()
    }

    private fun loadChatsData(){
        viewModelScope.launch(Dispatchers.IO) {
            val userChats: List<UserChat> = repository.getAllChats()

            val mappedItems = userChats.map { userChat ->
                when (userChat.flag) {
                    LocalChatFlag.CHAT_USER.flag -> ChatItem.ChatUser(userChat.message ?: "")
                    LocalChatFlag.CHAT_MODEL.flag -> ChatItem.ChatResponse(userChat.message ?: "")
                    LocalChatFlag.CHAT_VIEW_TASK.flag -> ChatItem.ChatGetTask(
                        taskName = userChat.taskName ?: "",
                        taskDesc = userChat.taskDesc ?: "",
                        taskDateTime = userChat.taskDateTime ?: ""
                    )
                    LocalChatFlag.CHAT_ADD_TASK.flag -> ChatItem.ChatAddTask(
                        roomDbId = userChat.id,
                        taskName = userChat.taskName ?: "",
                        taskDesc = userChat.taskDesc ?: "",
                        taskDateTime = userChat.taskDateTime ?: "",
                        actionCompleted = userChat.isTaskActionCompleted ?: false
                    )
                    LocalChatFlag.CHAT_EDIT_TASK.flag -> ChatItem.ChatEditTask(
                        roomDbId = userChat.id,
                        taskId = userChat.taskId ?: "",
                        taskName = userChat.taskName ?: "",
                        taskDesc = userChat.taskDesc ?: "",
                        taskDateTime = userChat.taskDateTime ?: "",
                        actionCompleted = userChat.isTaskActionCompleted ?: false
                    )
                    LocalChatFlag.CHAT_DELETE_TASK.flag -> ChatItem.ChatDeleteTask(
                        roomDbId = userChat.id,
                        taskId = userChat.taskId ?: "",
                        taskName = userChat.taskName ?: "",
                        taskDesc = userChat.taskDesc ?: "",
                        taskDateTime = userChat.taskDateTime ?: "",
                        actionCompleted = userChat.isTaskActionCompleted ?: false
                    )
                    LocalChatFlag.CHAT_WEB.flag -> ChatItem.ChatWebSearch(
                        url = userChat.searchUrl ?: ""
                    )
                    else -> throw IllegalArgumentException("Unknown chat flag: ${userChat.flag}")
                }
            }

            withContext(Dispatchers.Main) {
                _chatItems.value = mappedItems
            }
        }
    }

    fun insertNewChat(flag: Int, message: String? = null, task: Task? = null, url: String?=null) {
        viewModelScope.launch(Dispatchers.IO) {
            var newChat : UserChat? = null
            when(flag) {
                LocalChatFlag.CHAT_USER.flag -> newChat = UserChat(flag = flag, message = message)
                LocalChatFlag.CHAT_MODEL.flag -> newChat = UserChat(flag = flag, message = message)
                LocalChatFlag.CHAT_VIEW_TASK.flag -> newChat = UserChat(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                )
                LocalChatFlag.CHAT_ADD_TASK.flag -> newChat = UserChat(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_EDIT_TASK.flag -> newChat = UserChat(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_DELETE_TASK.flag -> newChat = UserChat(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_WEB.flag -> newChat = UserChat(flag = flag, searchUrl = url)

            }

            repository.insertChat(newChat!!)
            loadChatsData()
        }
    }

    fun clearAllChats() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleleAllChat()
            loadChatsData()
        }
    }

    fun confirmTaskAction(dbId : Int){
        viewModelScope.launch(Dispatchers.IO) {
            repository.confirmAction(dbId)
        }
    }
}