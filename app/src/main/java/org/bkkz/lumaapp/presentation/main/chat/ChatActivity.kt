package org.bkkz.lumaapp.presentation.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.data.local.UserChat
import org.bkkz.lumaapp.data.local.UserChatDao
import org.bkkz.lumaapp.presentation.main.chat.adapter.ChatAdapter
import org.koin.android.ext.android.inject
import org.bkkz.lumaapp.presentation.main.chat.voice_chat.VoiceChatActivity
import org.bkkz.lumaapp.util.component.chat.ChatItem

class ChatActivity : AppCompatActivity() {

    private val userChatDao: UserChatDao by inject()

    //UI
    private lateinit var newChatBtn: ImageView
    private lateinit var recyclerChats: RecyclerView
    private lateinit var btnVoice: ImageButton
    private lateinit var imgNoChat: ImageView
    private lateinit var txtNoChat: TextView

    private var userChats: List<UserChat>? = null

    // MOCK DATA
    private val mockData: List<ChatItem> = listOf(
        ChatItem.ChatUser("ขอดูงานทั้งหมดวันที่ 5 กันยาหน่อย"),
        ChatItem.ChatResponse("นี่คืองานทั้งหมดครับ"),
        ChatItem.ChatGetTask("ประชุมอัพเดตงาน", "-", "4 Sep 2025 | 18.00"),
        ChatItem.ChatUser("เพิ่มจัดตารางเรียน"),
        ChatItem.ChatResponse("ได้เลย แต่วันนี้มีอยู่แล้วนะครับ"),
        ChatItem.ChatAddTask("จัดตารางเรียน", "-", "4 Sep 2025 | 18.00", false),
        ChatItem.ChatUser("แก้ลบทานอาหารค่ำเป็น 30 กค 18.30 ให้หน่อย"),
        ChatItem.ChatResponse("ได้เลยครับ แต่มีงานนี้เยอะนะครับ"),
        ChatItem.ChatEditTask("xxx","ทานอาหารค่ำ","-", "4 Sep 2025 | 19.00", false),
        ChatItem.ChatDeleteTask("xxx","ทานอาหารค่ำ","-", "4 Sep 2025 | 19.00", false),
        ChatItem.ChatUser("หาข้อมูลเรื่อง Shio Pan ให้หน่อย"),
        ChatItem.ChatWebSearch("https://iel.co.th/%E0%B8%8A%E0%B8%B4%E0%B9%82%E0%B8%AD%E0%B8%B0%E0%B8%9B%E0%B8%B1%E0%B8%87/")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        setupData()
        findView()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupData() {
        lifecycleScope.launch {
            userChats = withContext(Dispatchers.IO) {
                userChatDao.getAllUserChat()
            }
        }

    }

    private fun findView() {
        newChatBtn = findViewById(R.id.imgview_chat_new_btn)
        recyclerChats = findViewById(R.id.recyclerview_chat)
        btnVoice = findViewById(R.id.imgbtn_chat_mic)
        imgNoChat = findViewById(R.id.imgview_chat_new_mascot)
        txtNoChat = findViewById(R.id.txtview_chat_new_desc)
    }

    private fun setupView() {
        //TODO: CHANGE TO DATA FROM ROOM LATER
        if (mockData.isNullOrEmpty()) {
            recyclerChats.visibility = View.GONE
            imgNoChat.visibility = View.VISIBLE
            txtNoChat.visibility = View.VISIBLE
        } else {
            recyclerChats.visibility = View.VISIBLE
            imgNoChat.visibility = View.GONE
            txtNoChat.visibility = View.GONE
        }
        recyclerChats.layoutManager = LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, false)
        recyclerChats.adapter = ChatAdapter(mockData)
    }

    private fun setupEvents() {
        btnVoice.setOnClickListener {
            startActivity(
                Intent(
                    this@ChatActivity,
                    VoiceChatActivity::class.java
                )
            )
        }
        newChatBtn.setOnClickListener {
            userChats = null
        }
    }
}