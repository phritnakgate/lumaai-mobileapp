package org.bkkz.lumaapp.presentation.main.task.edit_task

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.task.Task
import org.bkkz.lumaapp.presentation.main.task.edit_task.state.EditTaskEvent
import org.bkkz.lumaapp.presentation.main.task.edit_task.state.EditTaskState
import org.bkkz.lumaapp.util.LabelEditText
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.dialog.TwoActionDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.bkkz.lumaapp.util.enums.TaskCategory
import org.bkkz.lumaapp.util.enums.TaskPriority
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class EditTaskActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel: EditTaskViewModel by viewModel()

    //UI
    private lateinit var lbledtTaskName: LabelEditText
    private lateinit var lblTaskDesc: EditText
    private lateinit var chkboxTime: CheckBox
    private lateinit var chkboxTimeText: TextView
    private lateinit var edtDate: EditText
    private lateinit var edtTime: EditText
    private lateinit var backBtn: ImageView
    private lateinit var categorySelector: Spinner
    private lateinit var prioritySelector: Spinner
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var editTaskBtn: AppCompatButton
    private lateinit var deleteTaskBtn: AppCompatButton
    private var oldTask: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_task)

        findView()
        setupData()
        setupView()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupData() {
        oldTask =
            intent.getParcelableExtra("TASK_DATA", Task::class.java)
        viewModel.onEvent(EditTaskEvent.InitData(oldTask!!))
        lbledtTaskName.text = oldTask!!.name
        lblTaskDesc.setText(oldTask!!.description)

    }

    private fun findView() {
        lbledtTaskName = findViewById(R.id.lbledt_edit_task_name)
        lblTaskDesc = findViewById(R.id.edttxt_edit_task_desc)
        chkboxTime = findViewById(R.id.chkbox_edit_task_chkbox)
        chkboxTimeText = findViewById(R.id.txtview_edit_task_chkbox)
        edtDate = findViewById(R.id.edttxt_edit_task_date)
        edtTime = findViewById(R.id.edttxt_edit_task_time)
        backBtn = findViewById(R.id.imgview_edit_task_back)
        categorySelector = findViewById(R.id.spinner_edit_task_category)
        prioritySelector = findViewById(R.id.spinner_edit_task_priority)
        editTaskBtn = findViewById(R.id.compatbtn_edit_task)
        deleteTaskBtn = findViewById(R.id.compatbtn_delete_task)
        loadingDialog = LoadingDialog(this@EditTaskActivity)
    }

    private fun setupView() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                setupName(state.errorField[EditTaskState.RequiredFormField.TASK_NAME])
                setupDesc()
                chkboxTime.isChecked = state.isTimeSpecify
                if (state.isTimeSpecify) {
                    edtDate.setText(state.taskDate)

                    edtDate.setBackgroundResource(
                        if (state.errorField[EditTaskState.RequiredFormField.TASK_DATE] == true)
                            R.drawable.edit_text_bg_danger else R.drawable.edit_text_bg
                    )
                    edtDate.setOnClickListener { showDatePicker() }
                    edtTime.setText(state.taskTime)
                    edtTime.setBackgroundResource(
                        if (state.errorField[EditTaskState.RequiredFormField.TASK_TIME] == true)
                            R.drawable.edit_text_bg_danger else R.drawable.edit_text_bg
                    )
                    edtTime.setOnClickListener { showTimePicker() }
                } else {
                    edtDate.setBackgroundResource(R.drawable.edit_text_bg_disabled)
                    edtTime.setBackgroundResource(R.drawable.edit_text_bg_disabled)
                    edtDate.setOnClickListener { null }
                    edtTime.setOnClickListener { null }
                    edtDate.setText(state.taskDate)
                    edtTime.setText(state.taskTime)
                }
                Log.d("EditTaskActivity", "Cat: ${state.category} Pr: ${state.priority}")
                setupCategorySelector(state.category ?: TaskCategory.OTHERS.value)
                setupPrioritySelector(state.priority ?: TaskPriority.HIGH.value)


                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
                when (state.serviceState) {
                    ServiceState.IDLE -> {}
                    ServiceState.LOADING -> {
                        loadingDialog.show()
                    }

                    ServiceState.SUCCESS -> {
                        OneActionDialog(this@EditTaskActivity).show(
                            drawable = R.drawable.ic_dialog_success,
                            title = getString(R.string.edit_task_success_dialog),
                            message = "",
                            onConfirmClickListener = {
                                    finish()
                            },
                        )
                    }

                    ServiceState.FAILED -> {
                        OneActionDialog(this@EditTaskActivity).show(
                            drawable = R.drawable.ic_dialog_no,
                            title = getString(R.string.edit_task_failed_dialog_title),
                            message = viewModel.state.value.serviceMessage ?: "",
                            onConfirmClickListener = {
                                viewModel.setIdle()
                            },
                        )
                    }
                }
            }
        }

    }

    private fun setupEvents() {
        chkboxTime.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onEvent(EditTaskEvent.OnCheckTimeSpecified(isChecked))
        }
        backBtn.setOnClickListener {
            finish()
        }
        editTaskBtn.setOnClickListener {
            viewModel.onEvent(EditTaskEvent.OnEditTask)
        }
        deleteTaskBtn.setOnClickListener {
            TwoActionDialog(this@EditTaskActivity).show(
                drawable = R.drawable.ic_dialog_warning,
                title = getString(R.string.delete_task_confirm_dialog_title),
                message = "",
                onConfirmClickListener = {
                    viewModel.onEvent(EditTaskEvent.OnDeleteTask)
                },
                onAbortClickListener = {}
            )
        }


    }

    private fun setupName(isError: Boolean? = null) {
        if (isError == true) {
            lbledtTaskName.setError(true)
        } else {
            lbledtTaskName.setError(false)
        }
        lbledtTaskName.onTextChanged { text, start, before, count ->
            viewModel.onEvent(EditTaskEvent.OnChangeName(text.toString()))
        }
    }

    private fun setupDesc() {
        lblTaskDesc.doOnTextChanged { text, start, before, count ->
            viewModel.onEvent(
                EditTaskEvent.OnChangeDescription(
                    text.toString()
                )
            )
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
            val requestSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            viewModel.onEvent(EditTaskEvent.OnSelectedDate(requestSdf.format(calendar.time)))
        }
        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+07:00"))
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText(getString(R.string.select_time))
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            viewModel.onEvent(
                EditTaskEvent.OnSelectedTime(
                    "${
                        timePicker.hour.toString().padStart(2, '0')
                    }:${timePicker.minute.toString().padStart(2, '0')}"
                )
            )
        }
        timePicker.show(supportFragmentManager, "MATERIAL_TIME_PICKER")
    }

    private fun setupCategorySelector(id: Int) {
        val categories = TaskCategory.entries.map { it.displayName }
        val categoryAdapter =
            ArrayAdapter(this@EditTaskActivity, R.layout.spinner_layout, categories)
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item)
        categorySelector.adapter = categoryAdapter
        categorySelector.setSelection(id)
        categorySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selectedCategory = TaskCategory.fromInt(p2) ?: TaskCategory.OTHERS
                Log.d(
                    "EditTaskActivity",
                    "Selected category: ${selectedCategory.displayName} (${selectedCategory.value})"
                )
                viewModel.onEvent(EditTaskEvent.OnSelectedCategory(selectedCategory.value))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun setupPrioritySelector(id: Int) {
        val priorities = TaskPriority.entries.map { it.displayName }
        val priorityAdapter =
            ArrayAdapter(this@EditTaskActivity, R.layout.spinner_layout, priorities)
        priorityAdapter.setDropDownViewResource(R.layout.spinner_item)
        prioritySelector.adapter = priorityAdapter
        prioritySelector.setSelection(id)
        prioritySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                val selectedPriority = TaskPriority.fromInt(p2) ?: TaskPriority.HIGH
                Log.d(
                    "EditTaskActivity",
                    "Selected priority: ${selectedPriority.displayName} (${selectedPriority.value})"
                )
                viewModel.onEvent(EditTaskEvent.OnSelectedPriority(selectedPriority.value))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }
}