package org.bkkz.lumaapp.presentation.main.task.add_task

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
import org.bkkz.lumaapp.util.LabelEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class AddTaskActivity : AppCompatActivity() {

    private lateinit var lbledtTaskName : LabelEditText
    private lateinit var lblTaskDesc : EditText
    private lateinit var chkboxTime : CheckBox
    private lateinit var edtDate : EditText
    private lateinit var edtTime : EditText
    private lateinit var backBtn : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

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
        lbledtTaskName = findViewById(R.id.lbledt_add_task_name)
        lblTaskDesc = findViewById(R.id.edttxt_add_task_desc)
        chkboxTime = findViewById(R.id.chkbox_add_task_chkbox)
        edtDate = findViewById(R.id.edttxt_add_task_date)
        edtTime = findViewById(R.id.edttxt_add_task_time)
        backBtn = findViewById(R.id.imgview_add_task_back)
    }
    private fun setupView(){}
    private fun setupEvents(){
        chkboxTime.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                edtDate.setBackgroundResource(R.drawable.edit_text_bg)
                edtDate.setOnClickListener {
                    showDatePicker()
                }
                edtTime.setBackgroundResource(R.drawable.edit_text_bg)
                edtTime.setOnClickListener {
                    showTimePicker()
                }
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
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
            calendar.timeInMillis = selection
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            edtDate.setText(sdf.format(calendar.time))
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }
    private fun showTimePicker(){
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.select_time))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            edtTime.setText("${timePicker.hour}:${timePicker.minute}")
        }
        timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

}