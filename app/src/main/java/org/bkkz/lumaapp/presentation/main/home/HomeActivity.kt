package org.bkkz.lumaapp.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.auth.LandingActivity
import org.bkkz.lumaapp.presentation.main.chat.ChatActivity
import org.bkkz.lumaapp.presentation.main.chat_history.ChatHistoryActivity
import org.bkkz.lumaapp.presentation.main.home.state.HomeEvent
import org.bkkz.lumaapp.presentation.main.report.ReportActivity
import org.bkkz.lumaapp.presentation.main.task.view_task.ViewTaskActivity
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryListAdapter
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryListDecoration
import org.bkkz.lumaapp.util.component.chat_history.ReadAllHistoryBottomSheet
import org.bkkz.lumaapp.util.enums.ServiceState
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity(), ChatHistoryListAdapter.OnChatHistoryListener {

    //ViewModel
    private val viewModel : HomeViewModel by viewModel()

    //UI
    private lateinit var logoutBtn : ConstraintLayout
    private lateinit var talkBtn : ConstraintLayout
    private lateinit var taskBtn : ConstraintLayout
    private lateinit var formBtn : ConstraintLayout
    private lateinit var seeChatHistory : TextView
    private lateinit var recyclerViewRecentChats : RecyclerView
    private lateinit var loadingAnimation : LottieAnimationView
    private lateinit var noRecentChatsImg : ImageView
    private lateinit var noRecentChatsTxt : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        findViews()
        setupViews()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun findViews(){
        logoutBtn = findViewById(R.id.constraintlayout_home_logout_btn)
        talkBtn = findViewById(R.id.constraintlayout_home_chat_btn)
        taskBtn = findViewById(R.id.constraintlayout_home_task_btn)
        formBtn = findViewById(R.id.constraintlayout_home_form_btn)
        seeChatHistory = findViewById(R.id.txtview_home_history_see_all)
        recyclerViewRecentChats = findViewById(R.id.recyclerview_home_history_recent)
        loadingAnimation = findViewById(R.id.lottie_home_loading)
        noRecentChatsImg = findViewById(R.id.imgview_home_no_recent_history)
        noRecentChatsTxt = findViewById(R.id.txtview_home_no_recent_history)
    }
    private fun setupViews(){
        recyclerViewRecentChats.layoutManager =
            LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
        recyclerViewRecentChats.addItemDecoration(ChatHistoryListDecoration(this@HomeActivity, true))

        viewModel.onEvent(HomeEvent.OnLoadRecent)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when(state.serviceState){
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> {
                        noRecentChatsImg.visibility = View.GONE
                        noRecentChatsTxt.visibility = View.GONE
                        loadingAnimation.visibility = View.VISIBLE
                    }
                    ServiceState.SUCCESS -> {
                        loadingAnimation.visibility = View.GONE
                        recyclerViewRecentChats.adapter = ChatHistoryListAdapter(true,state.recentChats, this@HomeActivity)
                        if(state.recentChats.isEmpty()){
                            recyclerViewRecentChats.visibility = View.GONE
                            noRecentChatsImg.visibility = View.VISIBLE
                            noRecentChatsTxt.visibility = View.VISIBLE
                        }else{
                            recyclerViewRecentChats.visibility = View.VISIBLE
                            noRecentChatsImg.visibility = View.GONE
                            noRecentChatsTxt.visibility = View.GONE
                        }
                    }
                    ServiceState.FAILED -> {
                        loadingAnimation.visibility = View.GONE
                        noRecentChatsImg.visibility = View.VISIBLE
                        noRecentChatsTxt.visibility = View.VISIBLE
                    }
                }
            }
        }

    }
    private fun setupEvents(){
        setupLogoutBtn()
        taskBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ViewTaskActivity::class.java))
        }
        seeChatHistory.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ChatHistoryActivity::class.java))
        }
        talkBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ChatActivity::class.java))
        }
        formBtn.setOnClickListener {
            startActivity(Intent(this@HomeActivity, ReportActivity::class.java))
        }
    }

    private fun setupLogoutBtn(){
        logoutBtn.setOnClickListener {
            lifecycleScope.launch {
                viewModel.logout()
                val sharedPrefs = getSharedPreferences("userSession", MODE_PRIVATE)
                sharedPrefs.edit { clear() }

                val credentialManager = CredentialManager.create(this@HomeActivity)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())

                val intent = Intent(this@HomeActivity, LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
    }

    override fun onReadAllClicked(fullText: String) {
        val bottomSheet = ReadAllHistoryBottomSheet.newInstance(fullText)
        bottomSheet.show(supportFragmentManager, "FullTextBottomSheetFragment")
    }
}