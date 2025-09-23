package org.bkkz.lumaapp.presentation.main.task.view_task

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.task.view_task.adapter.ViewTaskPagerAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date

class ViewTaskActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel : ViewTaskViewModel by viewModel()

    //UI
    private lateinit var homeBtn : ImageView
    private lateinit var tabLayout : TabLayout
    private lateinit var taskView : ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_task)

        findView()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view_task)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findView(){
        homeBtn = findViewById(R.id.imgview_view_task_home)
        tabLayout = findViewById(R.id.tablayout_view_task)
        taskView = findViewById(R.id.viewpager_view_task)
    }
    private fun setupView(){
        val pagerAdapter = ViewTaskPagerAdapter(this@ViewTaskActivity)
        taskView.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, taskView) {tab, pos ->
            tab.text = when(pos) {
                0 -> getString(R.string.view_task_menu_daily)
                1 -> getString(R.string.view_task_menu_monthly)
                else -> null
            }
        }.attach()

        val formatted = SimpleDateFormat("yyyy-MM-dd").format(Date())
        Log.d("ViewTaskActivity",formatted)

        lifecycleScope.launch {
            viewModel.getAllUserTask(formatted)
        }


    }
    private fun setupEvents(){
        homeBtn.setOnClickListener {
            finish()
        }
    }
}