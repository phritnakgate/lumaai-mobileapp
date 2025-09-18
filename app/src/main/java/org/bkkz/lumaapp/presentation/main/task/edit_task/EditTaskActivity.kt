package org.bkkz.lumaapp.presentation.main.task.edit_task

import android.os.Build
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.util.LabelEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class EditTaskActivity : AppCompatActivity() {

    private lateinit var lbledtTaskName : LabelEditText
    private lateinit var lblTaskDesc : EditText
    private lateinit var chkboxTime : CheckBox
    private lateinit var edtDate : EditText
    private lateinit var edtTime : EditText
    private lateinit var backBtn : ImageView

    private var oldTask : Task? = null
    private val selectedCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_task)

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

    private fun setupData(){
        oldTask = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("TASK_DATA", Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("TASK_DATA")
        }
    }

    private fun findView(){
        lbledtTaskName = findViewById(R.id.lbledt_edit_task_name)
        lblTaskDesc = findViewById(R.id.edttxt_edit_task_desc)
        chkboxTime = findViewById(R.id.chkbox_edit_task_chkbox)
        edtDate = findViewById(R.id.edttxt_edit_task_date)
        edtTime = findViewById(R.id.edttxt_edit_task_time)
        backBtn = findViewById(R.id.imgview_edit_task_back)
    }
    private fun setupView(){
        oldTask?.let { task ->
            lbledtTaskName.text = task.name
            lblTaskDesc.setText(task.description)
            chkboxTime.isChecked = task.dateTime.isNotEmpty()
            if(task.dateTime.isNotEmpty()){
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("GMT+07:00")
                val date = isoFormat.parse(task.dateTime)
                date?.let {
                    selectedCalendar.time = it
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    edtDate.setText(dateFormat.format(selectedCalendar.time))
                    edtDate.setBackgroundResource(R.drawable.edit_text_bg)
                    edtDate.setOnClickListener { showDatePicker() }
                    edtTime.setText(timeFormat.format(selectedCalendar.time))
                    edtTime.setBackgroundResource(R.drawable.edit_text_bg)
                    edtTime.setOnClickListener { showTimePicker() }
                }
            }
        }

    }
    private fun setupEvents(){
        chkboxTime.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                edtDate.setBackgroundResource(R.drawable.edit_text_bg)
                edtDate.setOnClickListener { showDatePicker() }
                edtTime.setBackgroundResource(R.drawable.edit_text_bg)
                edtTime.setOnClickListener { showTimePicker() }
            }else{
                edtDate.setBackgroundResource(R.drawable.edit_text_bg_disabled)
                edtTime.setBackgroundResource(R.drawable.edit_text_bg_disabled)
                edtDate.setOnClickListener { null }
                edtTime.setOnClickListener { null }
                edtDate.setText("")
                edtTime.setText("")
            }
        }
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker(){
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(selectedCalendar.timeInMillis)
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            selectedCalendar.timeInMillis = selection

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
            calendar.timeInMillis = selection
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            edtDate.setText(sdf.format(calendar.time))
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }
    private fun showTimePicker(){
        val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedCalendar.get(Calendar.MINUTE)
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.select_time))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        timePicker.addOnPositiveButtonClickListener {

            selectedCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            selectedCalendar.set(Calendar.MINUTE, timePicker.minute)

            edtTime.setText("${timePicker.hour}:${timePicker.minute}")
        }
        timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }
}