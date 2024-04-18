package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.core.graphics.toRectF
import com.polestar.base.utils.logd
import kotlin.math.roundToInt

class DashLineSpan(
    lineColor: Int = Color.parseColor("#000000"),
    lineStrokeWidth: Float = 4f
) : ReplacementSpan() {
    private val linePaint = Paint().also {
        it.color = lineColor
        it.strokeWidth = lineStrokeWidth
    }

    var size = 0


    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        size = paint.measureText(text.toString(), start, end).roundToInt()
        return size
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

        logd("draw start:$start, end:$end", "_____")
        // draw text
        canvas.drawText(text.toString(), start, end, x, y.toFloat(), paint)


        val centerY = top + ((bottom-top)/2f)
        canvas.drawLine(x, centerY, x+size, centerY, linePaint)
    }

}