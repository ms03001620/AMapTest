package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint

class TagText(
    val scale: Float,
    val tagTextColor: Int,
) {
    private lateinit var paintScale: Paint
    private val scaleTextRect = Rect()

    fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ) {
        paintScale = TextPaint(paint)
        paintScale.textSize = paint.textSize * scale
        paintScale.color = tagTextColor
        paintScale.textAlign = Paint.Align.CENTER
        paintScale.getTextBounds(text.toString(), start, end, scaleTextRect)
    }

    fun drawTagText(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        bgRectF: RectF,
    ) {
        canvas.drawText(
            text.toString(),
            start,
            end,
            bgRectF.centerX(),
            bgRectF.centerY() + scaleTextRect.height() / 2,
            paintScale
        )
    }
}