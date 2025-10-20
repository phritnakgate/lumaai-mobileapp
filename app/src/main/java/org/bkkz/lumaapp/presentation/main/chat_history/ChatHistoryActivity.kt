package org.bkkz.lumaapp.presentation.main.chat_history

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.presentation.main.chat_history.adapter.ChatHistoryAdapter
import org.bkkz.lumaapp.presentation.main.chat_history.state.ChatHistoryEvent
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryItem
import org.bkkz.lumaapp.util.component.chat_history.ReadAllHistoryBottomSheet
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ChatHistoryActivity : AppCompatActivity(), ChatHistoryAdapter.OnHistoryInteractionListener {

    //ViewModel
    private val viewModel : ChatHistoryViewModel by viewModel()

    //UI
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var backBtn: ImageView
    private lateinit var selectTask: TextView
    private lateinit var selectSearch: TextView
    private lateinit var selectPlan: TextView
    private lateinit var selectGenForm: TextView
    private lateinit var edtSearch: EditText
    private lateinit var dateSearchBtn: ImageView
    private lateinit var recyclerChat: RecyclerView
    private lateinit var imgViewNoChat: ImageView
    private lateinit var txtViewNoChat: TextView
    private lateinit var loadingDialog: LoadingDialog

    private lateinit var categoryViews: Map<String, TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_history)

        findViews()
        setupViews()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findViews() {
        rootLayout = findViewById(R.id.main)
        backBtn = findViewById(R.id.imgview_chat_history_back)
        selectTask = findViewById(R.id.txtview_chat_history_task)
        selectSearch = findViewById(R.id.txtview_chat_history_search)
        selectPlan = findViewById(R.id.txtview_chat_history_plan)
        selectGenForm = findViewById(R.id.txtview_chat_history_genform)
        edtSearch = findViewById(R.id.edttxt_chat_history_search)
        dateSearchBtn = findViewById(R.id.imgview_chat_history_date)
        recyclerChat = findViewById(R.id.recyclerview_chat_history)
        imgViewNoChat = findViewById(R.id.imgview_chat_history_no_history)
        txtViewNoChat = findViewById(R.id.txtview_chat_history_no_history)
        loadingDialog = LoadingDialog(this@ChatHistoryActivity)

        categoryViews = mapOf(
            CATEGORY_TASK to selectTask,
            CATEGORY_SEARCH to selectSearch,
            CATEGORY_PLAN to selectPlan,
            CATEGORY_GENFORM to selectGenForm
        )
    }

    private fun setupViews() {
        recyclerChat.layoutManager = LinearLayoutManager(this@ChatHistoryActivity, RecyclerView.VERTICAL, false)
        viewModel.onEvent(ChatHistoryEvent.OnLoadFirstTimeChatHistory)
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if(loadingDialog.isShowing) loadingDialog.dismiss()
                when(state.serviceState){
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> {loadingDialog.show()}
                    ServiceState.SUCCESS -> {loadingDialog.dismiss()}
                    ServiceState.FAILED -> {}
                }
                updateFilter()
            }
        }

    }

    private fun setupEvents() {
        rootLayout.setOnClickListener {
            hideKeyboard()
        }
        categoryViews.forEach { (category, textView) ->
            textView.setOnClickListener {
                viewModel.onEvent(ChatHistoryEvent.SelectChatHistoryType(category))
            }
        }

        backBtn.setOnClickListener {
            finish()
        }

        dateSearchBtn.setOnClickListener {
            showDatePicker()
        }

        edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                val keyword = edtSearch.text.toString().ifBlank { null }
                viewModel.onEvent(ChatHistoryEvent.OnQueryByKeyword(keyword))
                true

            } else {
                false
            }

        }
    }

    private fun hideKeyboard(){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        edtSearch.clearFocus()
    }

    private fun updateFilter() {
        val category = viewModel.state.value.currentChatHistoryPage
        categoryViews.forEach { (key, view) ->
            setSelected(view, key == category)
        }
        when(category){
            CATEGORY_TASK -> {
                val recyclerData = groupHistoryForAdapter(category, viewModel.state.value.chatHistoryTask)
                recyclerChat.adapter = ChatHistoryAdapter(recyclerData, this@ChatHistoryActivity)
                updateNoHistoryView(recyclerData.isEmpty())
            }
            CATEGORY_SEARCH -> {
                val recyclerData = groupHistoryForAdapter(category, viewModel.state.value.chatHistorySearch)
                recyclerChat.adapter = ChatHistoryAdapter(recyclerData, this@ChatHistoryActivity)
                updateNoHistoryView(recyclerData.isEmpty())
            }
            CATEGORY_PLAN -> {
                val recyclerData = groupHistoryForAdapter(category, viewModel.state.value.chatHistoryPlan)
                recyclerChat.adapter = ChatHistoryAdapter(recyclerData, this@ChatHistoryActivity)
                updateNoHistoryView(recyclerData.isEmpty())
            }
            CATEGORY_GENFORM -> {
                val recyclerData = groupHistoryForAdapter(category, viewModel.state.value.chatHistoryGenForm)
                recyclerChat.adapter = ChatHistoryAdapter(recyclerData, this@ChatHistoryActivity)
                updateNoHistoryView(recyclerData.isEmpty())
            }
        }

    }

    private fun updateNoHistoryView(isEmpty: Boolean) {
        val visibility = if (isEmpty) View.VISIBLE else View.GONE
        imgViewNoChat.visibility = visibility
        txtViewNoChat.visibility = visibility
        recyclerChat.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun setSelected(view: TextView, isSelected: Boolean) {
        if (isSelected) {
            view.setTextColor(getColor(R.color.white))
            view.setBackgroundResource(R.drawable.rect_primary)
        } else {
            view.setTextColor(getColor(R.color.black))
            view.setBackgroundResource(R.drawable.rect_bdcolor)
        }
    }
    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
            calendar.timeInMillis = selection
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            viewModel.onEvent(ChatHistoryEvent.OnQueryByDate(sdf.format(calendar.time)))
        }
        datePicker.addOnNegativeButtonClickListener {
            viewModel.onEvent(ChatHistoryEvent.OnQueryByDate(null))
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun groupHistoryForAdapter(intent: String, histories: List<ChatHistory>): List<ChatHistoryItem> {
        val items = mutableListOf<ChatHistoryItem>()
        var lastDate: String? = null

        val filteredAndSorted = histories
            .filter { it.intent.contains(intent) }
            .sortedByDescending { it.timeStamp }

        filteredAndSorted.forEach { history ->
            val currentDate = history.timeStamp.substring(0, 10)
            if (currentDate != lastDate) {
                items.add(ChatHistoryItem.ChatHistoryDate(currentDate))
                lastDate = currentDate
            }
            val chatContent = listOf(
                if(intent == CATEGORY_GENFORM) "ทำการสร้างฟอร์มเรียบร้อย" else history.modelResponse,
                history.userText
            )
            items.add(ChatHistoryItem.ChatHistoryLists(chatContent))
        }

        return items
    }

    override fun onShowBottomSheet(fullText: String) {
        val bottomSheet = ReadAllHistoryBottomSheet.newInstance(fullText)
        bottomSheet.show(supportFragmentManager, "FullTextBottomSheetFragment")
    }

    companion object {
        const val CATEGORY_TASK = "Task"
        const val CATEGORY_SEARCH = "Search"
        const val CATEGORY_PLAN = "Plan"
        const val CATEGORY_GENFORM = "GenForm"
    }
}