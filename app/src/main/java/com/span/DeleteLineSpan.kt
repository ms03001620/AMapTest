package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.style.ReplacementSpan

/**
 * 删除线span
 *
 * 支持设置删除线颜色，宽度，和位置微调
 * 系统的[android.text.style.StrikethroughSpan]可用于简单的删除线样式设置
 */
class DeleteLineSpan(
    lineColor: Int = Color.parseColor("#FF0000"),
    lineStrokeWidth: Float = 2f,
    private val offsetOfCenterY: Float = 0f,
) : ReplacementSpan() {

    private val linePaint = Paint().also {
        it.color = lineColor
        it.strokeWidth = lineStrokeWidth
    }
    private var width = 0

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        width = Math.round(paint.measureText(text, start, end))
        val metrics = paint.fontMetricsInt
        if (fm != null) {
            fm.top = metrics.top
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.bottom = metrics.bottom
        }
        return width
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
        canvas.drawText(text.toString(), start, end, x, y.toFloat(), paint)

        val centerY = top + ((bottom - top) / 2f) + offsetOfCenterY
        canvas.drawLine(x, centerY, x + width, centerY, linePaint)
    }

}