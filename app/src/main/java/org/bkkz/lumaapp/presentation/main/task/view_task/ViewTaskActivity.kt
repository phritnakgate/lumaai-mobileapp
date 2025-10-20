package org.bkkz.lumaapp.presentation.main.task.view_task

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.task.view_task.adapter.ViewTaskPagerAdapter
import org.bkkz.lumaapp.presentation.main.task.view_task.state.ViewTaskEvent
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewTaskActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel : ViewTaskViewModel by viewModel()

    //UI
    private lateinit var homeBtn : ImageView
    private lateinit var tabLayout : TabLayout
    private lateinit var taskView : ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_task)
        enableEdgeToEdge()

        findView()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view_task)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.onEvent(ViewTaskEvent.LoadFirstTimeTasks)
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

        viewModel.onEvent(ViewTaskEvent.LoadFirstTimeTasks)

    }
    private fun setupEvents(){
        homeBtn.setOnClickListener {
            finish()
        }
    }
}