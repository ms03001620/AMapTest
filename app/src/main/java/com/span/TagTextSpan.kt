package com.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

class TagTextSpan(
    scale: Float = 1f,
    textColor: Int? = null,
    bgColor: Int,
    bgMargin: Rect? = null,
) : ReplacementSpan() {
    private val tagText = TagText(scale, textColor, bgMargin, bgColor)

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return tagText.getSize(paint, text, start, end, fm)
    }

    private fun drawOriginText(
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
        tagText.drawTagText(canvas, text, start, end, x, top, y, bottom, paint)
    }
}