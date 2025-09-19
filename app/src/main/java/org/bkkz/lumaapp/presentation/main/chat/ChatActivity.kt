package org.bkkz.lumaapp.presentation.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.chat.adapter.ChatAdapter
import org.bkkz.lumaapp.presentation.main.chat.voice_chat.VoiceChatActivity
import org.bkkz.lumaapp.util.enums.LocalChatFlag
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModel()

    //UI
    private lateinit var backBtn : ImageView
    private lateinit var newChatBtn: ImageView
    private lateinit var recyclerChats: RecyclerView
    private lateinit var edtChat : EditText
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSend : ImageButton
    private lateinit var imgNoChat: ImageView
    private lateinit var txtNoChat: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupData()
        findView()
        setupView()
        setupEvents()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom.coerceAtLeast(ime.bottom)
            )
            insets
        }
    }

    private fun setupData() {
        viewModel.chatItems.observe(this@ChatActivity){ userChats ->
            if (userChats.isNullOrEmpty()) {
                recyclerChats.visibility = View.GONE
                imgNoChat.visibility = View.VISIBLE
                txtNoChat.visibility = View.VISIBLE
            } else {
                recyclerChats.visibility = View.VISIBLE
                imgNoChat.visibility = View.GONE
                txtNoChat.visibility = View.GONE
            }
            val adapter = ChatAdapter(userChats, onConfirmClick = {dbId -> viewModel.confirmTaskAction(dbId)})
            recyclerChats.layoutManager = LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, false)
            recyclerChats.adapter = adapter
            recyclerChats.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun findView() {
        backBtn = findViewById(R.id.imgview_chat_back)
        newChatBtn = findViewById(R.id.imgview_chat_new_btn)
        recyclerChats = findViewById(R.id.recyclerview_chat)
        edtChat = findViewById(R.id.edttxt_chat)
        btnVoice = findViewById(R.id.imgbtn_chat_mic)
        btnSend = findViewById(R.id.imgbtn_chat_send)
        imgNoChat = findViewById(R.id.imgview_chat_new_mascot)
        txtNoChat = findViewById(R.id.txtview_chat_new_desc)
    }

    private fun setupView() {
        recyclerChats.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerChats.postDelayed({
                    val adapter = recyclerChats.adapter
                    if (adapter != null && adapter.itemCount > 0) {
                        recyclerChats.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }, 100)
            }
        }
    }

    private fun setupEvents() {

        backBtn.setOnClickListener {
            finish()
        }

        edtChat.setOnClickListener {

        }

        btnVoice.setOnClickListener {
            startActivity(
                Intent(
                    this@ChatActivity,
                    VoiceChatActivity::class.java
                )
            )
        }
        btnSend.setOnClickListener {
            viewModel.insertNewChat(LocalChatFlag.CHAT_USER.flag,edtChat.text.toString())
            //MOCK MODEL RESPONSE CHANGE TO REAL SERVICE LATER
            viewModel.insertNewChat(LocalChatFlag.CHAT_MODEL.flag,"ตอบกลับมาแล้วครับ")
            val r = (2..6).random()
            val mockTask = Task(
                "-OZNle77lJsusGm0CrFD",
                "ประชุมงานประจำเดือน",
                "postman :D",
                "2025-09-05T14:27:11.2037297+07:00",
                false,
                "p6W1pVygPBgKgYB77yqpEw8Hx8B2")
            val mockUrl = "https://www.wongnai.com/recipes/ugc/6256334b980d4b05818d9a5e9d45bccc"
            when(r){
                2 -> viewModel.insertNewChat(flag=LocalChatFlag.CHAT_VIEW_TASK.flag, task = mockTask)
                3 -> viewModel.insertNewChat(flag=LocalChatFlag.CHAT_ADD_TASK.flag, task = mockTask)
                4 -> viewModel.insertNewChat(flag=LocalChatFlag.CHAT_EDIT_TASK.flag, task = mockTask)
                5 -> viewModel.insertNewChat(flag=LocalChatFlag.CHAT_DELETE_TASK.flag, task = mockTask)
                6 -> viewModel.insertNewChat(flag=LocalChatFlag.CHAT_WEB.flag, url = mockUrl)
            }
            edtChat.text.clear()
        }
        newChatBtn.setOnClickListener {
            viewModel.clearAllChats()
        }
    }
}