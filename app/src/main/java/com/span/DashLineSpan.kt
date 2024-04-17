package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.core.graphics.toRectF
import kotlin.math.roundToInt

class DashLineSpan(
    lineColor: Int = Color.parseColor("#000000"),
    lineStrokeWidth: Float = 4f
) : ReplacementSpan() {
    private val linePaint = Paint().also {
        it.color = lineColor
        it.strokeWidth = lineStrokeWidth
    }
    private lateinit var rectF: RectF

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val tagWidth = paint.measureText(text.toString(), start, end)
        val rect = Rect()
        paint.getTextBounds(text.toString(), start, end, rect)
        rectF = rect.toRectF()
        return tagWidth.roundToInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        // draw text
        canvas.drawText(text.toString(), start, end, x, y.toFloat(), paint)

        // draw dash
        rectF.offset(x, y.toFloat())
        drawDashLine(canvas, rectF)
    }

    private fun drawDashLine(canvas: Canvas, rectF: RectF) {
        val y = rectF.centerY()
        canvas.drawLine(rectF.left, y, rectF.right, y, linePaint)
    }
}