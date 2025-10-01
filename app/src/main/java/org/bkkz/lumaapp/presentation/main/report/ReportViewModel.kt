package org.bkkz.lumaapp.presentation.main.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bkkz.lumaapp.data.Repository
import org.bkkz.lumaapp.data.local.UserReportEntity
import org.bkkz.lumaapp.data.remote.ApiResult
import org.bkkz.lumaapp.presentation.main.report.state.ReportActivityEvent
import org.bkkz.lumaapp.presentation.main.report.state.ReportActivityState
import org.bkkz.lumaapp.util.enums.ServiceState
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ReportViewModel(private val repository: Repository) : ViewModel() {

    private val _state : MutableStateFlow<ReportActivityState> = MutableStateFlow(ReportActivityState())
    val state : StateFlow<ReportActivityState> = _state.asStateFlow()

    fun onEvent(event: ReportActivityEvent){
        when(event){
            is ReportActivityEvent.OnGenerateMonthlyReport -> {
                _state.update { it.copy(serviceState = ServiceState.LOADING) }
                getMonthlyReport(event.context,event.reportYM)
            }
        }
    }

    fun getMonthlyReport(context: Context, reportYM : String){
        viewModelScope.launch {
            try {
                val result = repository.generateMisTaskReport(reportYM)
                when(result){
                    is ApiResult.Success -> {
                        val filePath = withContext(Dispatchers.IO) {
                            val connection = URL(result.data).openConnection()
                            connection.connect()
                            val inputStream = connection.getInputStream()

                            val fileName = "monthly_task_report_$reportYM.pdf"
                            val file = File(context.cacheDir, fileName)

                            FileOutputStream(file).use { outputStream ->
                                inputStream.use { it.copyTo(outputStream) }
                            }

                            repository.insertUserReport(UserReportEntity(
                                fileNameKey = fileName,
                                localFilePath = file.path,
                                downloadedTimeStamp = System.currentTimeMillis()
                            ))

                            file.path
                        }
                        _state.update {
                            it.copy(
                                serviceState = ServiceState.SUCCESS,
                                recentGeneratedFilePath = filePath
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(serviceState = ServiceState.FAILED) }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}