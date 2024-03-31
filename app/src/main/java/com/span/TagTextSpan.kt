package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

class TagTextSpan(
    val bgColor: Int = Color.parseColor("#FF7500"),
    val scale: Float = 1f,
    val tagTextColor: Int = Color.parseColor("#FFFFFF"),
) : ReplacementSpan() {
    private var width = 0.0f
    private var tagWidth = 0.0f
    private lateinit var paintScale: Paint
    private val scaleTextRect = Rect()
    val tagBackground = TagBackground(bgColor)

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        width = paint.measureText(text.toString(), 0, text.toString().length)
        tagWidth = paint.measureText(text.toString(), start, end)

        paintScale = TextPaint(paint)
        paintScale.textSize = paint.textSize * scale
        paintScale.color = tagTextColor
        paintScale.textAlign = Paint.Align.CENTER
        paintScale.getTextBounds(text.toString(), start, end, scaleTextRect)

        tagBackground.getSize(width, tagWidth)

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
        tagBackground.drawBg(canvas = canvas, top = top, bottom = bottom)
        drawOriginText(canvas, text, start, end, x, top, y, bottom, paint)
        drawTagText(canvas, text, start, end, tagBackground.getBgRect(), paintScale)
    }

    private fun drawTagText(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        bgRectF: RectF,
        paintScale: Paint
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
}