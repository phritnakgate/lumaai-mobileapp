package org.bkkz.lumaapp.presentation.main.report

import android.os.Bundle
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
            viewModel.onEvent(ReportActivityEvent.OnGenerateMonthlyReport(this@ReportActivity, "2025-09"))
        }
    }
    
}