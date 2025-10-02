package org.bkkz.lumaapp.presentation.main.report

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.PdfViewerActivity.Companion.ENABLE_FILE_DOWNLOAD
import com.rajat.pdfviewer.util.saveTo
import kotlinx.coroutines.launch
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.presentation.main.report.state.ReportActivityEvent
import org.bkkz.lumaapp.util.dialog.LoadingDialog
import org.bkkz.lumaapp.util.dialog.OneActionDialog
import org.bkkz.lumaapp.util.enums.ServiceState
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar


class ReportActivity : AppCompatActivity() {

    //ViewModel
    private val viewModel : ReportViewModel by viewModel()
    //UI
    private lateinit var createMonthlyReportBtn : ConstraintLayout
    private lateinit var reportHistories : RecyclerView
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        findView()
        setupViews()
        setupEvents()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findView() {
        createMonthlyReportBtn = findViewById(R.id.constraintlayout_report_create_monthly)
        reportHistories = findViewById(R.id.recyclerview_report_history)
        loadingDialog = LoadingDialog(this@ReportActivity)
    }
    private fun setupViews() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if(loadingDialog.isShowing){ loadingDialog.dismiss()}
                when(state.serviceState){
                    ServiceState.LOADING -> {
                        loadingDialog.show()
                    }
                    ServiceState.IDLE -> {}
                    ServiceState.SUCCESS -> {
                        val pdfViewerActivity = PdfViewerActivity.launchPdfFromPath(
                            context = this@ReportActivity,
                            path = state.recentGeneratedFilePath!!,
                            pdfTitle = getString(R.string.report_type_0),
                            saveTo = saveTo.ASK_EVERYTIME,
                            fromAssets = false
                        )
                        pdfViewerActivity.putExtra(ENABLE_FILE_DOWNLOAD, true)
                        startActivity(pdfViewerActivity)

                    }
                    ServiceState.FAILED -> {
                        OneActionDialog(this@ReportActivity).show(
                            drawable = R.drawable.ic_dialog_no,
                            title = "Failed",
                            message = "Failed to generate report. Please try again later.",
                            onConfirmClickListener = {}
                        )
                    }
                }
            }
        }
    }
    private fun setupEvents() {
        createMonthlyReportBtn.setOnClickListener {
            selectMonthYearDialog()
        }
    }


    private fun selectMonthYearDialog(){

        val dialogView = LayoutInflater.from(this@ReportActivity).inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.numpicker_monthpicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.numpicker_yearpicker)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues = arrayOf(
            getString(R.string.month_1_cut),
            getString(R.string.month_2_cut),
            getString(R.string.month_3_cut),
            getString(R.string.month_4_cut),
            getString(R.string.month_5_cut),
            getString(R.string.month_6_cut),
            getString(R.string.month_7_cut),
            getString(R.string.month_8_cut),
            getString(R.string.month_9_cut),
            getString(R.string.month_10_cut),
            getString(R.string.month_11_cut),
            getString(R.string.month_12_cut)
        )
        monthPicker.value = Calendar.getInstance().get(Calendar.MONTH) + 1
        for(i in 0 until monthPicker.childCount){
            val child = monthPicker.getChildAt(i)
            if(child is EditText){
                child.setTextAppearance(R.style.LumaAI_TextAppearance_BodyMedium_Eng)
            }
        }

        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = thisYear - 50
        yearPicker.maxValue = thisYear + 50
        yearPicker.value = thisYear

        for(i in 0 until yearPicker.childCount){
            val child = monthPicker.getChildAt(i)
            if(child is EditText){
                child.setTextAppearance(R.style.LumaAI_TextAppearance_BodyMedium_Eng)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val selectedMonth = monthPicker.value
                val selectedYear = yearPicker.value
                val monthString = if(selectedMonth < 10) "0$selectedMonth" else "$selectedMonth"
                val reportYM = "$selectedYear-$monthString"
                viewModel.onEvent(ReportActivityEvent.OnGenerateMonthlyReport(this@ReportActivity, reportYM))
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn.setTextAppearance(R.style.LumaAI_TextAppearance_BodyMedium_Eng)
            positiveBtn.setTextColor(getColor(R.color.primary))
        }

        dialog.show()
    }
    
}