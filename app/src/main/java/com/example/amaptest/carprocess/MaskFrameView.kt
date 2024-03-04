package com.example.amaptest.carprocess

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import com.example.amaptest.R
import com.example.amaptest.ui.main.dp

class MaskFrameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var widthDp = 0
    private var heightDp = 0

    private var process: Float = 0.0f
    private var paintMask = Paint()
    private var paddingStartOfCar = 35.dp
    private var paddingEndOfCar = 25.dp

    private val carDrawable: Drawable
    private val processLayout: ProcessLayout
    private val stickLayout: StickLayout
    private val textLayout: TextLayout
    private val maskDrawable: Drawable
    private lateinit var maskImage: Bitmap

    init {
        carDrawable = context.getDrawable(R.drawable.charging_bg_car)!!
        processLayout = ProcessLayout(getContext(), paddingStartOfCar, paddingEndOfCar)
        stickLayout = StickLayout(getContext(), paddingStartOfCar, paddingEndOfCar)
        textLayout = TextLayout(getContext(), paddingStartOfCar, paddingEndOfCar)

        maskDrawable = context.getDrawable(R.drawable.charging_bg_car_single)!!
        paintMask.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        widthDp = wSpecSize
        heightDp = hSpecSize
        println("widthDp:$widthDp")

        carDrawable.setBounds(0, 0, widthDp, heightDp)
        maskDrawable.setBounds(0, 0, widthDp, heightDp)

        maskImage = maskDrawable.toBitmap(widthDp, heightDp)

        processLayout.onMeasure(widthDp, heightDp)
        stickLayout.onMeasure(widthDp, heightDp)
        textLayout.onMeasure(widthDp, heightDp)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        carDrawable.draw(canvas)

        val sc = canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            null,
            Canvas.ALL_SAVE_FLAG
        )

        processLayout.onDraw(canvas, process)
        stickLayout.onDraw(canvas, process)
        textLayout.onDraw(canvas, process)

        canvas.drawBitmap(maskImage, 0f, 0f, paintMask)
        canvas.restoreToCount(sc)
    }

    fun process(process: Float) {
        this.process = process
        invalidate()

        stickLayout.startAnimation(process) {
            invalidate()
        }
    }
}