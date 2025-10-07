package org.bkkz.lumaapp.presentation.main.chat

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.entity.task.CreateTaskRequest
import org.bkkz.lumaapp.data.entity.task.EditTaskRequest
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.data.local.UserChatEntity
import org.bkkz.lumaapp.data.local.UserReportEntity
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.util.component.chat.ChatItem
import org.bkkz.lumaapp.util.enums.LLMIntent
import org.bkkz.lumaapp.util.enums.LocalChatFlag
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel(private val repository: Repository) : ViewModel() {

    private val _chatItems = MutableLiveData<List<ChatItem>>()
    val chatItems: LiveData<List<ChatItem>> = _chatItems

    private var requiredTask : Task? = null

    init {
        loadChatsData()
    }

    private fun loadChatsData(){
        viewModelScope.launch(Dispatchers.IO) {
            val userChatEntities: List<UserChatEntity> = repository.getAllChats()

            val mappedItems = userChatEntities.map { userChat ->
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
                        actionCompleted = userChat.isTaskActionCompleted ?: false,
                        taskCategory = userChat.taskCategory ?: 0,
                        taskPriority = userChat.taskPriority ?: 0
                    )
                    LocalChatFlag.CHAT_DELETE_TASK.flag -> ChatItem.ChatDeleteTask(
                        roomDbId = userChat.id,
                        taskId = userChat.taskId ?: "",
                        taskName = userChat.taskName ?: "",
                        taskDesc = userChat.taskDesc ?: "",
                        taskDateTime = userChat.taskDateTime ?: "",
                        actionCompleted = userChat.isTaskActionCompleted ?: false,
                        taskCategory = userChat.taskCategory ?: 0,
                        taskPriority = userChat.taskPriority ?: 0
                    )
                    LocalChatFlag.CHAT_WEB.flag -> ChatItem.ChatWebSearch(
                        url = userChat.searchUrl ?: ""
                    )
                    LocalChatFlag.CHAT_GENFORM.flag -> ChatItem.ChatGenForm(
                        url = userChat.generatedFormUrl ?: ""
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
            var newChat : UserChatEntity? = null
            when(flag) {
                LocalChatFlag.CHAT_USER.flag -> newChat = UserChatEntity(flag = flag, message = message)
                LocalChatFlag.CHAT_MODEL.flag -> newChat = UserChatEntity(flag = flag, message = message)
                LocalChatFlag.CHAT_VIEW_TASK.flag -> newChat = UserChatEntity(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                )
                LocalChatFlag.CHAT_ADD_TASK.flag -> newChat = UserChatEntity(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_EDIT_TASK.flag -> newChat = UserChatEntity(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_DELETE_TASK.flag -> newChat = UserChatEntity(
                    flag = flag,
                    taskId = task?.id,
                    taskName = task?.name,
                    taskDesc = task?.description,
                    taskDateTime = task?.dateTime,
                    isTaskActionCompleted = false
                )
                LocalChatFlag.CHAT_WEB.flag -> newChat = UserChatEntity(flag = flag, searchUrl = url)
                LocalChatFlag.CHAT_GENFORM.flag -> newChat = UserChatEntity(flag = flag, generatedFormUrl = url)

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

    fun confirmTaskAction(dbId : Int,flag: Int, task: Task){
        viewModelScope.launch(Dispatchers.IO) {
            repository.confirmAction(dbId)
            val outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            when(flag){
                LocalChatFlag.CHAT_ADD_TASK.flag -> {
                    val createTaskRequest = CreateTaskRequest(
                        name = requiredTask!!.name,
                        description = requiredTask!!.description,
                        dueDate = OffsetDateTime.parse(requiredTask!!.dateTime).format(outputDateFormatter),
                        dueTime = OffsetDateTime.parse(requiredTask!!.dateTime).format(outputTimeFormatter),
                        category = 0,
                        priority = 0
                    )
                    repository.createTask(createTaskRequest)
                }
                LocalChatFlag.CHAT_EDIT_TASK.flag -> {

                    val editTaskRequest = EditTaskRequest(
                        name = requiredTask!!.name.ifEmpty { null },
                        dateTime = requiredTask!!.dateTime.ifEmpty { null }
                    )
                    repository.editTask(task.id, editTaskRequest)
                }
                LocalChatFlag.CHAT_DELETE_TASK.flag -> {
                    repository.deleteTask(task.id)
                }
            }
        }
    }

    fun chatWithLuma(context: Context, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            insertNewChat(LocalChatFlag.CHAT_USER.flag, message)
            insertNewChat(LocalChatFlag.CHAT_MODEL.flag, "LUMA กำลังคิด...")
            try {
                val response = repository.chatWithLuma(message)
                when(response){
                    is ApiResult.Success -> {
                        val response = response.data
                        if(response?.errors.isNullOrEmpty()){
                            insertNewChat(LocalChatFlag.CHAT_MODEL.flag, response?.result)
                            var curInd = 0
                            response?.results?.forEach {
                                val isLast = curInd == (response.results.size - 1)
                                val nextInd = if(!isLast) curInd + 1 else curInd
                                if(it.intent == LLMIntent.CHECK.intent && response.results[nextInd].intent !in listOf(LLMIntent.ADD.intent, LLMIntent.EDIT.intent, LLMIntent.DELETE.intent)){
                                    val task = it.output
                                    task?.forEach { taskData ->
                                        if(taskData.id == "-1"){
                                            return@forEach
                                        }
                                        insertNewChat(LocalChatFlag.CHAT_VIEW_TASK.flag, task = taskData)
                                    }
                                }
                                if(it.intent == LLMIntent.GOOGLESEARCH.intent){
                                    insertNewChat(LocalChatFlag.CHAT_WEB.flag, url = it.message)
                                }
                                if(it.intent == LLMIntent.GENFORM.intent){
                                    val fileName = File(URL(it.message).path).name
                                    val file = File(context.cacheDir, fileName)

                                    val connection = URL(it.message).openConnection()
                                    connection.connect()
                                    val inputStream = connection.getInputStream()

                                    FileOutputStream(file).use { outputStream ->
                                        inputStream.use { inputStream ->
                                            inputStream.copyTo(outputStream)
                                        }
                                    }

                                    repository.insertUserReport(UserReportEntity(
                                        fileNameKey = fileName,
                                        localFilePath = file.path,
                                        downloadedTimeStamp = System.currentTimeMillis()
                                    ))
                                    insertNewChat(LocalChatFlag.CHAT_GENFORM.flag, url = file.path)
                                }
                                curInd += 1
                            }


                        }else{
                            response.errors.forEach {
                                insertNewChat(LocalChatFlag.CHAT_MODEL.flag, it.message)
                                if(!it.output.isNullOrEmpty()){
                                    when(it.intent){
                                        "ADD" -> {
                                            val size = it.output.size
                                            requiredTask = it.output[0]

                                            for(i in 1 until size){
                                                insertNewChat(LocalChatFlag.CHAT_VIEW_TASK.flag, task = it.output[i])
                                            }
                                            insertNewChat(LocalChatFlag.CHAT_ADD_TASK.flag, task = it.output[size - 1])

                                        }
                                        "EDIT" -> {
                                            val size = it.output.size
                                            requiredTask = it.output[0]
                                            for(i in 1 until size){
                                                insertNewChat(LocalChatFlag.CHAT_EDIT_TASK.flag, task = it.output[i])
                                            }
                                        }
                                        "REMOVE" -> {
                                            it.output.forEach { taskData ->
                                                insertNewChat(LocalChatFlag.CHAT_DELETE_TASK.flag, task = taskData)
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }
                    is ApiResult.Error -> {
                        insertNewChat(LocalChatFlag.CHAT_MODEL.flag, "ขออภัยครับ มีบางอย่างผิดพลาด ลองใหม่อีกครั้ง")
                    }
                }

            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    fun onViewDestroy() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.confirmAllAction()
        }
    }
}