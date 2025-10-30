package org.bkkz.lumaapp.presentation.main.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.chat.adapter.ChatAdapter
import org.bkkz.lumaapp.presentation.main.chat.adapter.QuickPromptAdapter
import org.bkkz.lumaapp.presentation.main.chat.voice_chat.VoiceChatActivity
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.isConnectedToInternet
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModel()

    //UI
    private lateinit var backBtn : ImageView
    private lateinit var newChatBtn: ImageView
    private lateinit var quickPromptBtn : ImageView
    private lateinit var recyclerChats: RecyclerView
    private lateinit var edtChat : EditText
    private lateinit var btnVoice: ImageButton
    private lateinit var btnSend : ImageButton
    private lateinit var imgNoChat: ImageView
    private lateinit var txtNoChat: TextView

    private lateinit var tts : TextToSpeech

    private val voiceChatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val spokenText = data?.getStringExtra(VoiceChatActivity.VOICE_RESULT)

            if (spokenText != null) {
                sendChats(this@ChatActivity, spokenText)
            }
        } else {
            Toast.makeText(this@ChatActivity, this.getString(R.string.chat_voice_failed), Toast.LENGTH_SHORT).show()
        }
    }

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

    override fun onStop() {
        super.onStop()
        tts.stop()
        viewModel.onViewDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    private fun setupData() {
        if(!this@ChatActivity.isConnectedToInternet()){
            OneActionDialog(this).show(
                drawable = R.drawable.ic_dialog_no,
                title = this.getString(R.string.no_internet_title),
                message = this.getString(R.string.no_internet_desc),
                onConfirmClickListener = {
                    finishAffinity()
                }
            )
        }
        tts = TextToSpeech(this, { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.forLanguageTag("th-TH")
            }
        }, "com.google.android.tts")
        viewModel.chatItems.observe(this@ChatActivity){ userChats ->
            btnVoice.alpha = 1.0f
            btnSend.alpha = 1.0f
            newChatBtn.alpha = 1.0f
            btnVoice.isEnabled = true
            btnSend.isEnabled = true
            newChatBtn.isEnabled = true
            if (userChats.isNullOrEmpty()) {
                recyclerChats.visibility = View.GONE
                imgNoChat.visibility = View.VISIBLE
                txtNoChat.visibility = View.VISIBLE
            } else {
                recyclerChats.visibility = View.VISIBLE
                imgNoChat.visibility = View.GONE
                txtNoChat.visibility = View.GONE
            }
            val adapter = ChatAdapter(userChats, onConfirmClick = {dbId,flag, task -> viewModel.confirmTaskAction(dbId,flag, task) }, tts)
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
        quickPromptBtn = findViewById(R.id.imgview_chat_qprompt_btn)
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

        btnVoice.setOnClickListener {
            voiceChatLauncher.launch(
                Intent(
                    this@ChatActivity,
                    VoiceChatActivity::class.java
                )
            )
        }
        btnSend.setOnClickListener {
            sendChats(this@ChatActivity, edtChat.text.toString())
        }
        quickPromptBtn.setOnClickListener { anchorView ->
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.popup_quick_prompt_lov, null)
            val popupWindow = PopupWindow(
                popupView,
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
            val recyclerQuickPrompt = popupView.findViewById<RecyclerView>(R.id.recyclerview_chat_lov_quick_prompt)
            val quickPromptAdapter = QuickPromptAdapter(viewModel.getQuickPromptAdapter(this@ChatActivity))
            quickPromptAdapter.onItemClick = { prompt ->
                edtChat.text = edtChat.text.insert(0, prompt)
                edtChat.setSelection(edtChat.text.length)
                Log.i("QuickPromptAdapter", "Selected Prompt: $prompt")
                popupWindow.dismiss()
            }
            recyclerQuickPrompt.layoutManager = LinearLayoutManager(this@ChatActivity, RecyclerView.VERTICAL, false)
            recyclerQuickPrompt.adapter = quickPromptAdapter
            popupWindow.isFocusable = true
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(anchorView, 0, (12f * resources.displayMetrics.density).toInt())
        }
        newChatBtn.setOnClickListener {
            viewModel.clearAllChats()
        }
    }

    private fun sendChats(context: Context, message: String){
        if(message.isEmpty() || message.isBlank()){
            return
        }
        lifecycleScope.launch {
            viewModel.chatWithLuma(context, message)
        }
        edtChat.text.clear()
        edtChat.clearFocus()
        btnVoice.alpha = 0.5f
        btnSend.alpha = 0.5f
        newChatBtn.alpha = 0.5f
        btnVoice.isEnabled = false
        btnSend.isEnabled = false
        newChatBtn.isEnabled = false
    }
}