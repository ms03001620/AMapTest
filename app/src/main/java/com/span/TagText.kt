package com.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint

class TagText(
    private val scale: Float,
    private val textColor: Int?,
    private val bgMargin: Rect? = null,
    private val bgColor: Int,
) {
    private lateinit var paintScale: Paint

    private val paintBg = Paint().also {
        it.color = bgColor
    }

    private val originTextRect = Rect()
    private val scaleTextRect = Rect()

    fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        paint.getTextBounds(text.toString(), start, end, originTextRect)

        paintScale = TextPaint(paint)
        paintScale.textSize = paint.textSize * scale
        textColor?.let {
            paintScale.color = it
        }
        paintScale.getTextBounds(text.toString(), start, end, scaleTextRect)

        return scaleTextRect.width() + (bgMargin?.left ?: 0) + (bgMargin?.right ?: 0)
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
        val h = (originTextRect.height() - scaleTextRect.height()) / 2

        val textLeft = x + (bgMargin?.left ?: 0)
        val textTop = y.toFloat() - h - scaleTextRect.height()
        val textRight = textLeft + scaleTextRect.width()
        val textBottom = y.toFloat() - h

        drawBg(canvas, textLeft, textTop, textRight, textBottom)
        canvas.drawText(text.toString(), start, end, textLeft, textBottom, paintScale)
    }

    private fun drawBg(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        bgMargin?.let {
            canvas.drawRect(
                left - bgMargin.left,
                top - bgMargin.top,
                right + bgMargin.right,
                bottom + bgMargin.bottom,
                paintBg
            )
        }
    }
}