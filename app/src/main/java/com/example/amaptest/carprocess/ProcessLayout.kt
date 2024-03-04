package com.example.amaptest.carprocess

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.example.amaptest.R
import kotlin.math.roundToInt

class ProcessLayout(
    context: Context,
    private val paddingStartOfCar: Int,
    private val paddingEndOfCar: Int
) {
    private lateinit var processImage: Bitmap
    private var processPaint: Paint
    private var processWidth = 0
    private val processDrawable: Drawable
    private var height = 0

    init {
        processDrawable = AppCompatResources.getDrawable(context, R.drawable.charging_process_green)
            ?: throw Resources.NotFoundException("charging_process_green")
        processPaint = Paint()
    }

    fun onMeasure(width: Int, height: Int) {
        this.height = height
        processDrawable.setBounds(0, 0, width, height)
        processWidth = width - paddingStartOfCar - paddingEndOfCar
        processImage = processDrawable.toBitmap(processWidth, height)
    }

    fun onDraw(canvas: Canvas, process: Float) {
        val w = (process * processWidth).roundToInt()
        if (w <= 0) {
            return
        }

        val bip = processDrawable.toBitmap(w, height)

        canvas.drawBitmap(
            bip,
            paddingStartOfCar.toFloat(),
            0f,
            processPaint
        )

        bip.recycle()
    }
}

