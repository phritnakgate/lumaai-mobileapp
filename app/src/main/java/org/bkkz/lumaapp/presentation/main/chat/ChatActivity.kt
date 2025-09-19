package org.bkkz.lumaapp.presentation.main.chat

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.chat.voice_chat.VoiceChatActivity

class ChatActivity : AppCompatActivity() {

    private lateinit var btnVoice : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        findView()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findView(){
        btnVoice = findViewById(R.id.imgbtn_chat_mic)
    }
    private fun setupView(){}
    private fun setupEvents(){
        btnVoice.setOnClickListener { startActivity(Intent(this@ChatActivity, VoiceChatActivity::class.java)) }
    }
}