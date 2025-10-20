package org.bkkz.lumaapp.presentation.main.task.add_task

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.task.add_task.state.AddTaskEvent
import org.bkkz.lumaapp.presentation.main.task.add_task.state.AddTaskState
import org.bkkz.lumaapp.util.LabelEditText
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.bkkz.lumaapp.util.enums.TaskCategory
import org.bkkz.lumaapp.util.enums.TaskPriority
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class AddTaskActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel : AddTaskViewModel by viewModel()


    //UI
    private lateinit var lbledtTaskName : LabelEditText
    private lateinit var lblTaskDesc : EditText
    private lateinit var chkboxTime : CheckBox
    private lateinit var edtDate : EditText
    private lateinit var edtTime : EditText
    private lateinit var backBtn : ImageView
    private lateinit var categorySelector : Spinner
    private lateinit var prioritySelector : Spinner
    private lateinit var createTaskBtn : AppCompatButton
    private lateinit var loadingDialog: LoadingDialog

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
        categorySelector = findViewById(R.id.spinner_add_task_category)
        prioritySelector = findViewById(R.id.spinner_add_task_priority)
        createTaskBtn = findViewById(R.id.compatbtn_add_task)
        loadingDialog = LoadingDialog(this@AddTaskActivity)
    }
    private fun setupView(){
        setupCategorySelector()
        setupPrioritySelector()
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                setupName(state.errorField[AddTaskState.RequiredFormField.TASK_NAME])
                lblTaskDesc.doOnTextChanged { text, start, before, count -> viewModel.onEvent(
                    AddTaskEvent.OnChangeDescription(text.toString())) }
                setupDate(state.errorField[AddTaskState.RequiredFormField.TASK_DATE], state.isTimeSpecify, state.taskDate ?: "")
                setupTime(state.errorField[AddTaskState.RequiredFormField.TASK_TIME], state.isTimeSpecify, state.taskTime ?: "")

                if(loadingDialog.isShowing){ loadingDialog.dismiss() }
                when(state.serviceState){
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> { loadingDialog.show() }
                    ServiceState.SUCCESS -> {
                        OneActionDialog(this@AddTaskActivity).show(
                            drawable = R.drawable.ic_dialog_success,
                            title = getString(R.string.create_task_success_dialog),
                            message = "",
                            onConfirmClickListener = {

                                finish()
                            },
                        )
                    }
                    ServiceState.FAILED -> {
                        OneActionDialog(this@AddTaskActivity).show(
                            drawable = R.drawable.ic_dialog_no,
                            title = getString(R.string.create_task_failed_dialog_title),
                            message = getString(R.string.create_task_failed_dialog_desc),
                            onConfirmClickListener = {
                                viewModel.setIdle()
                            },
                        )
                    }
                }
            }
        }
    }
    private fun setupEvents(){
        setTimeSpecifiedEvent()
        backBtn.setOnClickListener {
            finish()
        }
        createTaskBtn.setOnClickListener {
            viewModel.onEvent(AddTaskEvent.OnCreateTask)
        }
    }

    private fun setupCategorySelector(){
        val categories = TaskCategory.entries.map { it.displayName }
        val categoryAdapter = ArrayAdapter(this@AddTaskActivity, R.layout.spinner_layout, categories)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item)
        categorySelector.adapter = categoryAdapter
        categorySelector.setSelection(0)
        categorySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selectedCategory = TaskCategory.fromInt(p2) ?: TaskCategory.OTHERS
                Log.d("AddTaskActivity", "Selected category: ${selectedCategory.displayName} (${selectedCategory.value})")
                viewModel.onEvent(AddTaskEvent.OnSelectedCategory(selectedCategory.value))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun setupPrioritySelector(){
        val priorities = TaskPriority.entries.map { it.displayName }
        val priorityAdapter = ArrayAdapter(this@AddTaskActivity, R.layout.spinner_layout, priorities)
        priorityAdapter.setDropDownViewResource(R.layout.spinner_item)
        prioritySelector.adapter = priorityAdapter
        prioritySelector.setSelection(0)
        prioritySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selectedPriority = TaskPriority.fromInt(p2) ?: TaskPriority.HIGH
                Log.d("AddTaskActivity", "Selected priority: ${selectedPriority.displayName} (${selectedPriority.value})")
                viewModel.onEvent(AddTaskEvent.OnSelectedPriority(selectedPriority.value))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun setupName(isError: Boolean? = null){
        if(isError == true){
            lbledtTaskName.setError(true)
        }else{
            lbledtTaskName.setError(false)
        }
        lbledtTaskName.onTextChanged { text, start, before, count ->
            viewModel.onEvent(AddTaskEvent.OnChangeName(text.toString()))
        }
    }
    private fun setupDate(isError: Boolean? = null, isTimeSpecified : Boolean, displayText: String){
        if(!isTimeSpecified){
            edtDate.setBackgroundResource(R.drawable.edit_text_bg_disabled)
            edtDate.setOnClickListener { null }
            viewModel.onEvent(AddTaskEvent.OnSelectedDate(""))
        }else{
            edtDate.setBackgroundResource(R.drawable.edit_text_bg)
            edtDate.setOnClickListener {
                showDatePicker()
            }
            if(isError == true){
                edtDate.background = ContextCompat.getDrawable(this@AddTaskActivity, R.drawable.edit_text_bg_danger)
                edtDate.setTextColor(this@AddTaskActivity.getColor(R.color.danger))
            }else{
                edtDate.background = ContextCompat.getDrawable(this@AddTaskActivity, R.drawable.edit_text_bg)
                edtDate.setTextColor(this@AddTaskActivity.getColor(R.color.black))
            }
        }
        edtDate.setText(displayText)
    }
    private fun setupTime(isError: Boolean? = null, isTimeSpecified : Boolean, displayText: String){
        if(!isTimeSpecified){
            edtTime.setBackgroundResource(R.drawable.edit_text_bg_disabled)
            edtTime.setOnClickListener { null }
            viewModel.onEvent(AddTaskEvent.OnSelectedTime(""))
        }else{
            edtTime.setBackgroundResource(R.drawable.edit_text_bg)
            edtTime.setOnClickListener {
                showTimePicker()
            }
            if(isError == true){
                edtTime.background = ContextCompat.getDrawable(this@AddTaskActivity, R.drawable.edit_text_bg_danger)
                edtTime.setTextColor(this@AddTaskActivity.getColor(R.color.danger))
            }else{
                edtTime.background = ContextCompat.getDrawable(this@AddTaskActivity, R.drawable.edit_text_bg)
                edtTime.setTextColor(this@AddTaskActivity.getColor(R.color.black))
            }
        }
        edtTime.setText(displayText)
    }

    private fun setTimeSpecifiedEvent(){
        chkboxTime.setOnCheckedChangeListener{ _, isChecked ->
            viewModel.onEvent(AddTaskEvent.OnCheckTimeSpecified(isChecked))
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
            val requestSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            viewModel.onEvent(AddTaskEvent.OnSelectedDate(requestSdf.format(calendar.time)))
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
            viewModel.onEvent(AddTaskEvent.OnSelectedTime("${timePicker.hour.toString().padStart(2,'0')}:${timePicker.minute.toString().padStart(2,'0')}"))
        }
        timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

}