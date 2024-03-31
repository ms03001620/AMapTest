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
    val offsetY: Int = 0
) {
    private lateinit var paintScale: Paint

    private val originTextRect = Rect()
    private val scaleTextRect = Rect()

    fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ) {
        paint.getTextBounds(text.toString(), start, end, originTextRect)


        paintScale = TextPaint(paint)
        paintScale.textSize = paint.textSize * scale
        paintScale.color = tagTextColor
        paintScale.textAlign = Paint.Align.LEFT
        paintScale.getTextBounds(text.toString(), start, end, scaleTextRect)
    }

    fun drawTagText(
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

        val w = (originTextRect.width() - scaleTextRect.width())/2
        val h =  (originTextRect.height() - scaleTextRect.height())/2

        canvas.drawText(text.toString(), start, end, x+w, y.toFloat()-h, paintScale)

    }
}