package com.polestar.charging.ui.ring

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.polestar.base.ext.dp

class ProcessLayout(
    private val lineWith: Float = 12.dp.toFloat(),
    private val marginVertical: Float = 6.dp.toFloat(),
    private val bgColor: Int = Color.parseColor("#F3F3F3"),
    private val frontColor: Int = Color.parseColor("#4EFB61"),
    private val startProcessAngle: Float = -90f,
) {
    private var processPaint: Paint = createProcessPaint()
    private val bgPaint: Paint = createBackgroundPaint()
    private lateinit var bgRectF: RectF

    private fun createBackgroundPaint() = Paint().also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = lineWith
        it.color = bgColor
    }

    private fun createProcessPaint() = Paint().also {
        it.style = Paint.Style.STROKE
        it.strokeWidth = lineWith
        it.color = frontColor
        it.strokeCap = Paint.Cap.ROUND
    }

    fun onMeasure(width: Int, height: Int) {
        val radius = height - lineWith * 2 - marginVertical * 2
        val left = 0 + marginVertical
        val top = lineWith.toInt() + marginVertical
        val right = radius - marginVertical
        val bottom = radius + lineWith.toInt() - marginVertical

        bgRectF = RectF(left, top, right, bottom)
        val centerX = width / 2f - radius / 2
        bgRectF.offset(centerX, 0f + marginVertical)
    }

    fun onDraw(canvas: Canvas, process: Float) {
        // draw bg
        canvas.drawOval(bgRectF, bgPaint)

        // draw process
        val sweep = 360 * process
        canvas.drawArc(bgRectF, startProcessAngle, sweep, false, processPaint)
    }
}

