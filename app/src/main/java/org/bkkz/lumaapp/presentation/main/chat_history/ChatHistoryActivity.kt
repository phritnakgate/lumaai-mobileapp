package org.bkkz.lumaapp.presentation.main.chat_history

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.chat_history.ChatHistory
import org.bkkz.lumaapp.presentation.main.chat_history.adapter.ChatHistoryAdapter
import org.bkkz.lumaapp.util.component.chat_history.ChatHistoryItem
import org.bkkz.lumaapp.util.component.chat_history.ReadAllHistoryBottomSheet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ChatHistoryActivity : AppCompatActivity(), ChatHistoryAdapter.OnHistoryInteractionListener {

    //UI
    private lateinit var backBtn: ImageView
    private lateinit var selectTask: TextView
    private lateinit var selectSearch: TextView
    private lateinit var selectPlan: TextView
    private lateinit var edtSearch: EditText
    private lateinit var dateSearchBtn: ImageView
    private lateinit var recyclerChat: RecyclerView
    private lateinit var imgViewNoChat: ImageView
    private lateinit var txtViewNoChat: TextView

    private lateinit var categoryViews: Map<String, TextView>
    private var selectedCategory: String = CATEGORY_TASK

    // MOCK DATA
    private val mockData: List<ChatHistory> = listOf(
        ChatHistory(
            intent = listOf("Task"),
            userId = "p6W1pVygPBgKgYB77yqpEw8Hx8B2",
            userText = "เพิ่มจัดตารางเรียน",
            modelResponse = "เพิ่มจัดตารางเรียนเรียบร้อยแล้วครับ ต้องการให้ช่วยอะไรเพิ่มเติมอีกมั้ยครับ?",
            timeStamp = "2025-09-08T15:04:06.048004+07:00"
        ),
        ChatHistory(
            intent = listOf("Search"),
            userId = "p6W1pVygPBgKgYB77yqpEw8Hx8B2",
            userText = "หาข้อมูลเกี่ยวกับ IELTS",
            modelResponse = """IELTS หรือ International English Language Testing System เป็นการทดสอบความสามารถในการสื่อสารภาษาอังกฤษที่ใช้สำหรับผู้ที่ต้องการเข้าศึกษาในประเทศที่พูดภาษาอังกฤษเป็นภาษาหลัก หรือทำงานในบริษัทที่มีภาษาอังกฤษเป็นภาษาหลัก

IELTS มีสองรุ่นหลักคือ IELTS Academic และ IELTS General Training. IELTS Academic ออกแบบให้เหมาะสมสำหรับผู้ที่ต้องการเข้าศึกษาต่อในระดับอุดมศึกษา ในขณะที่ IELTS General Training ถูกสร้างขึ้นสำหรับผู้ที่ต้องการฝึกสื่อสารภาษาอังกฤษในบริบทประจำวัน

การทดสอบ IELTS จะประกอบไปด้วยส่วนทั้งหมด 4 ส่วน: Reading, Writing, Listening และ Speaking. แต่เนื้อหาของ Writing และ Speaking จะแตกต่างระหว่าง IELTS Academic และ IELTS General Training

คะแนนที่ได้จาก IELTS จะอยู่ในช่วง 0-9 และสามารถใช้เพื่อพิสูจน์ว่าคุณมีความรู้ความสามารถภาษาอังกฤษที่เหมาะสมสำหรับการศึกษา งานหรือย้ายเข้าประเทศต่างๆ""",
            timeStamp = "2025-09-08T14:36:00.0434245+07:00"
        ),
        ChatHistory(
            intent = listOf("Task", "Search"),
            userId = "p6W1pVygPBgKgYB77yqpEw8Hx8B2",
            userText = "เพิ่มจัดตารางและเพิ่มทานอาหารและหาขนมปังยอดฮิตในปี 2025 ",
            modelResponse = "เพิ่มจัดตารางและเพิ่มทานอาหารเรียบร้อยแล้วครับ และขนมปังยอดฮิตในปี 2025 จากข้อมูลที่ได้รับ มีแนวโน้มว่าจะเป็น \\\"Shio Pan\\\" หรือ ขนมปังเกลือ เนื่องจากมีหลายร้านที่แนะนำและให้ความสนใจ โดยเฉพาะร้าน Eric Kayser และ Jayeon Do Salted Bakery ต้องการให้ช่วยอะไรเพิ่มเติมอีกมั้ยครับ?",
            timeStamp = "2025-09-18T16:19:11.28558+07:00"
        )
    )


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
        backBtn = findViewById(R.id.imgview_chat_history_back)
        selectTask = findViewById(R.id.txtview_chat_history_task)
        selectSearch = findViewById(R.id.txtview_chat_history_search)
        selectPlan = findViewById(R.id.txtview_chat_history_plan)
        edtSearch = findViewById(R.id.edttxt_chat_history_search)
        dateSearchBtn = findViewById(R.id.imgview_chat_history_date)
        recyclerChat = findViewById(R.id.recyclerview_chat_history)
        imgViewNoChat = findViewById(R.id.imgview_chat_history_no_history)
        txtViewNoChat = findViewById(R.id.txtview_chat_history_no_history)

        categoryViews = mapOf(
            CATEGORY_TASK to selectTask,
            CATEGORY_SEARCH to selectSearch,
            CATEGORY_PLAN to selectPlan
        )
    }

    private fun setupViews() {
        recyclerChat.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        updateFilter(selectedCategory)
    }

    private fun setupEvents() {
        categoryViews.forEach { (category, textView) ->
            textView.setOnClickListener {
                updateFilter(category)
            }
        }

        backBtn.setOnClickListener {
            finish()
        }

        dateSearchBtn.setOnClickListener {
            showDatePicker()
        }
    }

    private fun updateFilter(category: String) {
        selectedCategory = category

        categoryViews.forEach { (key, view) ->
            setSelected(view, key == selectedCategory)
        }

        val recyclerData = groupHistoryForAdapter(selectedCategory, mockData)
        recyclerChat.adapter = ChatHistoryAdapter(recyclerData, this@ChatHistoryActivity)

        updateNoHistoryView(recyclerData.isEmpty())
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
            Toast.makeText(this, sdf.format(calendar.time), Toast.LENGTH_SHORT).show()
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
                history.modelResponse,
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
        private const val CATEGORY_TASK = "Task"
        private const val CATEGORY_SEARCH = "Search"
        private const val CATEGORY_PLAN = "Plan"
    }
}