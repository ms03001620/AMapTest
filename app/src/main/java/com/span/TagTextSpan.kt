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
    val scale: Float = .7f,
    val tagTextColor: Int = Color.parseColor("#FFFFFF"),
) : ReplacementSpan() {
    private var width = 0.0f
    private var tagWidth = 0.0f
    private val bgPaint = Paint()
    private lateinit var paintScale: Paint
    private val scaleTextRect = Rect()
    private val bgRectF = RectF()

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        bgPaint.setColor(bgColor)
        width = paint.measureText(text.toString(), 0, text.toString().length)
        tagWidth = paint.measureText(text.toString(), start, end)

        paintScale = TextPaint(paint)
        paintScale.textSize = paint.textSize * scale
        paintScale.color = tagTextColor
        paintScale.textAlign = Paint.Align.CENTER
        paintScale.getTextBounds(text.toString(), start, end, scaleTextRect)

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
        drawBg(canvas = canvas, top = top, bottom = bottom)
        drawOriginText(canvas, text, start, end, x, top, y, bottom, paint)
        drawTagText(canvas, text, start, end, bgRectF, paintScale)
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

    private fun drawBg(
        canvas: Canvas,
        top: Int,
        bottom: Int,
    ) {
        bgRectF.left = width - tagWidth
        bgRectF.top = top.toFloat()
        bgRectF.right = width
        bgRectF.bottom = bottom.toFloat()

        canvas.drawRect(bgRectF, bgPaint)
    }
}