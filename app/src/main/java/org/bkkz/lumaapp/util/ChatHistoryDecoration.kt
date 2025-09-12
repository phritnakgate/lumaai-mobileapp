package org.bkkz.lumaapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.bkkz.lumaapp.R

class ChatHistoryDecoration(
    context: Context,
    private val lineColor: Int = Color.WHITE,
    private val lineWidth: Float = 4f,
    private val circleRadius: Float = 24f
) : RecyclerView.ItemDecoration() {

    private val llmResponseColor = ContextCompat.getColor(context, R.color.llm_response_color)
    private val userTextColor = ContextCompat.getColor(context, R.color.llm_request_color)

    private val linePaint = Paint().apply {
        color = lineColor
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
    }

    private val circlePaint = Paint().apply {
        isAntiAlias = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            if (position == RecyclerView.NO_POSITION) {
                continue
            }

            val cx = parent.paddingLeft + circleRadius
            val cy = child.top + child.height / 2f

            // วาดเส้นเชื่อม
            if (position > 0) {
                c.drawLine(cx, child.top.toFloat(), cx, cy, linePaint)
            }

            // วาดเส้นจากวงกลมลงล่าง
            if (position < (parent.adapter?.itemCount ?: 0) - 1) {
                c.drawLine(cx, cy, cx, child.bottom.toFloat(), linePaint)
            }

            // กำหนดสีของวงกลม
            circlePaint.color = if (position % 2 == 0) llmResponseColor else userTextColor
            c.drawCircle(cx, cy, circleRadius, circlePaint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = (parent.paddingLeft + circleRadius * 2).toInt()
    }
}