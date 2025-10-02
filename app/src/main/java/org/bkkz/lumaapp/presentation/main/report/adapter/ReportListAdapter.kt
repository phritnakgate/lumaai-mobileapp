package org.bkkz.lumaapp.presentation.main.report.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.PdfViewerActivity.Companion.ENABLE_FILE_DOWNLOAD
import com.rajat.pdfviewer.util.saveTo
import org.bkkz.lumaapp.R
import org.bkkz.lumaapp.data.entity.report_history.ReportHistory
import org.bkkz.lumaapp.presentation.main.report.ReportViewModel
import org.bkkz.lumaapp.util.mapper.MonthStringMapper

class ReportListAdapter(
    private val viewModel: ReportViewModel,
    private val items: List<ReportHistory>
) : RecyclerView.Adapter<ReportListAdapter.ViewHolder>(){

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reportLayout : ConstraintLayout = view.findViewById(R.id.constraintlayout_recycler_report_list)
        val txtFileName: TextView = view.findViewById(R.id.txtview_recycler_report_list_title)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_report_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        Log.d("ReportListAdapter", "onBindViewHolder: ${items[position].fileName}, isCached: ${items[position].isCached}")
        val decoratedFileName = if(items[position].fileName.contains("monthly_task_report")){
            val reportName = holder.itemView.context.getString(R.string.report_type_0)
            val reportYM = items[position].fileName.split("_").lastOrNull() ?: ""
            val sepYM = reportYM.split("-").let {
                    if(it.size == 2) Pair(it[0], it[1]) else Pair("", "")
            }
            val monthText = MonthStringMapper.getString(holder.itemView.context, "month_${sepYM.second.toInt()}_full") ?: sepYM.second
            "$reportName ($monthText ${sepYM.first})"
        }
        else {items[position].fileName}


        holder.txtFileName.text = decoratedFileName
        holder.reportLayout.setOnClickListener {
           if(items[position].isCached){
               val pdfViewerActivity = PdfViewerActivity.launchPdfFromPath(
                   context = holder.itemView.context,
                   path = "${holder.itemView.context.cacheDir}/${items[position].fileName}.pdf",
                   pdfTitle = decoratedFileName,
                   saveTo = saveTo.ASK_EVERYTIME,
                   fromAssets = false
               )
               pdfViewerActivity.putExtra(ENABLE_FILE_DOWNLOAD, true)
               holder.itemView.context.startActivity(pdfViewerActivity)
           }else{
               viewModel.saveUncachedReport(holder.itemView.context, items[position].fileName, items[position].url)
               val pdfViewerActivity = PdfViewerActivity.launchPdfFromUrl(
                   context = holder.itemView.context,
                   pdfUrl = items[position].url,
                   pdfTitle = items[position].fileName,
                   saveTo = saveTo.ASK_EVERYTIME,
                   enableDownload = true
               )
               holder.itemView.context.startActivity(pdfViewerActivity)
           }

        }
    }

    override fun getItemCount(): Int = items.size
}