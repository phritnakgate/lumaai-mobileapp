package org.bkkz.lumaapp.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.local.TokenManager
import org.bkkz.lumaapp.data.remote.Repository
import org.bkkz.lumaapp.presentation.auth.LandingActivity
import org.bkkz.lumaapp.util.ChatHistoryAdapter
import org.bkkz.lumaapp.util.ChatHistoryDecoration

class HomeActivity : AppCompatActivity() {

    private lateinit var logoutBtn : ConstraintLayout
    private lateinit var talkBtn : ConstraintLayout
    private lateinit var taskBtn : ConstraintLayout
    private lateinit var formBtn : ConstraintLayout
    private lateinit var recyclerViewRecentChats : RecyclerView

    //MOCK CHAT DATA
    private val chatData = listOf(
        "เพิ่มประชุมเที่ยวเมืองกาญจน์ตอน 2 ทุ่ม เรียบร้อยครับ",
        "เพิ่มประชุมเที่ยวเมืองกาญจน์ตอน 2 ทุ่ม",
        "กำลังตรวจสอบให้ครับ... มีงานตามนี้ครับ",
        "ขอดูงานเมื่อวันที่ 15 หน่อย",
        "การวางแผนเที่ยวในสิงคโปร์ 3วัน2คืน อาจมีดังนี้\\n\\nวันที่1: \\n- เริ่มต้นที่กรุงเซ็นทารา โดยไปที่จัตุรัสเกียรติยศ\\n- แวะเที่ยวอิสระในศูนย์การค้าอย่าง เซ็นทรัล หรือ มารีน่าเบย์ เซ็นเตอร์\\n- สินใจเยี่ยมชมสถานที่สำคัญของประเทศ เช่น วัดเซนต์หลุยส์ หรืออนุสรณ์สถานอิสลามสิงคโปร์\\n- ปิดท้ายด้วยการชมแสงไฟที่อนุสาวรีย์Statue of Unity\\n\\nวันที่2:\\n- เริ่มต้นที่สวนพฤกษศาสตร์新加坡植物园\\n- แวะไปที่ศูนย์สัตว์เลี้ยงและสัตว์ป่าลุ่มน้ำแม่น้ำเซมา\\n- รับประทานอาหารกลางวันที่ตลาดอินเดียที่กรุงเซ็นทารา\\n- เยี่ยมชมสถานีรถไฟใต้ดินสิงคโปร์และห้างสรรพสินค้า\\n- ปิดท้ายด้วยการเยี่ยมชมช้อปปิ้งที่ศูนย์การค้าอีกครั้ง\\n\\nวันที่3:\\n- เริ่มต้นที่เมืองเก่าสิงคโปร์\\n- แวะเที่ยวที่ศูนย์วัฒนธรรมและประวัติศาสตร์\\n- รับประทานอาหารกลางวันที่ตลาดหุ่นเชื่อมที่กรุงเซ็นทารา\\n- เยี่ยมชมสถานีรถไฟใต้ดินสิงคโปร์และห้างสรรพสินค้า\\n- ปิดท้ายด้วยการเยี่ยมชมช้อปปิ้งที่ศูนย์การค้า\\n\\nหมายเหตุ: คำแนะนำเหล่านี้อาจ",
        "แพลนการเที่ยว สิงค์โปร 3วัน2คืน"
    )


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
        recyclerViewRecentChats = findViewById(R.id.recyclerview_home_history_recent)
    }
    private fun setupViews(){
        recyclerViewRecentChats.layoutManager = LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
        recyclerViewRecentChats.adapter = ChatHistoryAdapter(chatData)
        recyclerViewRecentChats.addItemDecoration(ChatHistoryDecoration(this@HomeActivity))
    }
    private fun setupEvents(){
        setupLogoutBtn()
    }

    private fun setupLogoutBtn(){
        logoutBtn.setOnClickListener {
            lifecycleScope.launch {
                Repository(TokenManager(applicationContext)).logout()
                val intent = Intent(this@HomeActivity, LandingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }
    }
}