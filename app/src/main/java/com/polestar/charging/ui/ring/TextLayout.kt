package com.polestar.charging.ui.ring

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.polestar.base.ext.dp
import kotlin.math.roundToInt

class TextLayout(
    val textColor: Int = Color.BLACK,
    val textSize: Float = 36.dp.toFloat()
) {
    private val textPaint: Paint = Paint()

    private var centerX = 0f
    private var centerY = 0f

    init {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
        textPaint.color = textColor
    }

    fun onMeasure(width: Int, height: Int) {
        centerX = width / 2f
        centerY = height / 2f

        val bounds = Rect()
        textPaint.getTextBounds("8", 0, 1, bounds)
        val height = bounds.height()
        centerY+=height/3
    }

    fun onDraw(canvas: Canvas, process: Float, isProcessTextEnable: Boolean) {
        if (isProcessTextEnable) {
            val processText = (process * 100).roundToInt()
            canvas.drawText("${processText}%", centerX, centerY, textPaint)
        }
    }
}