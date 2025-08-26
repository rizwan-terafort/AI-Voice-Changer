package com.voicechanger.funnysound.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class RecorderVisualizerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private data class Bar(val height: Float, val isSelected: Boolean)

    private val bars = mutableListOf<Bar>()
    private val scaleFactor = 100f
    private var scrollOffset = 0f

    private val selectedPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#705AF9") // Active color
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
        isAntiAlias = true
    }

    private val unselectedPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2F333C") // Gray inactive color
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
        isAntiAlias = true
    }

    fun addAmplitude(amp: Int) {
        val normalized = (amp / scaleFactor).coerceAtMost(height / 2f)
        // Decide selection based on amplitude or other logic
        val isSelected = true // mark all new bars as selected
        bars.add(Bar(normalized, isSelected))

        if (bars.size > 1000) bars.removeAt(0)
        scrollOffset += width.toFloat() / 50f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val middleY = height / 2f
        val barWidth = 10f
        val barSpace = 8f
        val totalWidth = bars.size * (barWidth + barSpace)

        if (bars.size < 100) {
            val startX = (width - totalWidth) / 2f
            drawBars(canvas, startX, middleY, barWidth, barSpace)
        } else {
            canvas.translate(-scrollOffset, 0f)
            drawBars(canvas, 0f, middleY, barWidth, barSpace)
        }
    }

    private fun drawBars(canvas: Canvas, startX: Float, middleY: Float, barWidth: Float, barSpace: Float) {
        var x = startX
        bars.forEach { bar ->
            val paintToUse = if (bar.isSelected) selectedPaint else unselectedPaint
            canvas.drawRoundRect(
                x,
                middleY - bar.height,
                x + barWidth,
                middleY + bar.height,
                20f,
                20f,
                paintToUse
            )
            x += (barWidth + barSpace)
        }
    }
}



