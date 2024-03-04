package com.example.amaptest.carprocess

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.example.amaptest.R
import kotlin.math.roundToInt

class ProcessLayout(
    context: Context,
    val paddingStartOfCar: Int,
    val paddingEndOfCar: Int
) {

    private lateinit var processImage: Bitmap
    private var processPaint: Paint
    private var processWidth = 0
    private val processDrawable: Drawable


    init {
        processDrawable = context.getDrawable(R.drawable.bg_green_gradient)!!
        processPaint = Paint()
    }

    fun onMeasure(width: Int, height: Int) {
        processDrawable.setBounds(0, 0, width, height)

        processWidth = width - paddingStartOfCar - paddingEndOfCar
        processImage = processDrawable.toBitmap(processWidth, height)
    }


    fun onDraw(canvas: Canvas, process: Float) {
        if (process < 0) {
            return
        }

        val xOffset = process * processWidth

        canvas.drawBitmap(
            processImage,
            paddingStartOfCar + xOffset - processWidth,
            0f,
            processPaint
        )
    }
}

