package com.example.amaptest.carprocess

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.amaptest.ui.main.dp
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
    }

    fun onDraw(canvas: Canvas, process: Float) {
        val processText = (process * 100).roundToInt()
        canvas.drawText("${processText}%", centerX, centerY, textPaint)
    }
}