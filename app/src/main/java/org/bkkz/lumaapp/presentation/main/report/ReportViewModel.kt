package org.bkkz.lumaapp.presentation.main.report

import android.content.Context
import android.util.Log
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
import org.bkkz.lumaapp.data.entity.report_history.ReportHistory
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
                createMonthlyReport(event.context,event.reportYM)
            }
            is ReportActivityEvent.LoadReportHistory -> {
                _state.update { it.copy(serviceState = ServiceState.LOADING) }
                getReportList("monthly_task_report")
            }
            is ReportActivityEvent.OnDeleteModeToggle -> {
                _state.update { it.copy(isDeleteMode = !it.isDeleteMode) }
            }
        }
    }

    fun createMonthlyReport(context: Context, reportYM : String){
        viewModelScope.launch {
            try {
                val result = repository.generateMisTaskReport(reportYM)
                when(result){
                    is ApiResult.Success -> {
                        try{
                            val filePath = downloadReportFile(context, "monthly_task_report_$reportYM", result.data)
                            _state.update {
                                it.copy(
                                    serviceState = ServiceState.SUCCESS,
                                    recentGeneratedFilePath = filePath
                                )
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                            _state.update { it.copy(serviceState = ServiceState.FAILED) }

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

    fun getReportList(formType: String){
        viewModelScope.launch {
            try{
                val result = repository.getReportHistory(formType)
                when(result){
                    is ApiResult.Success -> {
                        val resultList = arrayListOf<ReportHistory>()
                        result.data.results?.forEach { report ->
                            val cachedReport = repository.isReportCached(report.fileName)
                            Log.d("ReportViewModel", "getReportList: ${report.fileName} isCached: $cachedReport")
                            if(cachedReport){
                                resultList.add(ReportHistory(
                                    fileName = report.fileName,
                                    url = report.url,
                                    isCached = true
                                ))
                            }else{
                                resultList.add(report)
                            }
                        }
                        _state.update {
                            it.copy(
                                reportList = resultList,
                                serviceState = ServiceState.IDLE
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(serviceState = ServiceState.FAILED) }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun saveUncachedReport(context: Context, fileName: String ,url: String){
        viewModelScope.launch {
            val filePath = downloadReportFile(context, fileName, url)

            repository.insertUserReport(UserReportEntity(
                fileNameKey = fileName,
                localFilePath = filePath,
                downloadedTimeStamp = System.currentTimeMillis()
            ))

            val currentList = _state.value.reportList?.toMutableList() ?: mutableListOf()
            val index = currentList.indexOfFirst { it.fileName == fileName }
            if(index != -1){
                currentList[index] = ReportHistory(
                    fileName = fileName,
                    url = url,
                    isCached = true
                )
                _state.update {
                    it.copy(
                        reportList = currentList
                    )
                }
            }
        }
    }

    private suspend fun downloadReportFile(context: Context, fileName: String ,url: String) : String = withContext(Dispatchers.IO) {
        Log.d("ReportViewModel", "downloadReportFile: Downloading $fileName from $url")
        val connection = URL(url).openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()

        val file = File(context.cacheDir, fileName)

        if(file.exists()){
            file.delete()
        }

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

    fun deleteReportFile(formType: String, fileName: String) {
        _state.update { it.copy(serviceState = ServiceState.LOADING) }
        viewModelScope.launch {
            try{
                val response = repository.deleteReport(formType, fileName)
                when(response){
                    is ApiResult.Success -> {
                        getReportList(formType)
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(serviceState = ServiceState.FAILED) }
                    }
                }
            }catch (e: Exception){
                _state.update { it.copy(serviceState = ServiceState.FAILED) }
            }
        }

    }

}