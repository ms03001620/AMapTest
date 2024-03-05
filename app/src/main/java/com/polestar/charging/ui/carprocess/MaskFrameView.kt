package com.polestar.charging.ui.carprocess

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.example.amaptest.R
import com.polestar.base.ext.dp


class MaskFrameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var widthDp = 0
    private var heightDp = 0

    private var process: Float = 0.0f
    private var paintMask = Paint()
    private var paddingStartOfCar = 38.dp
    private var paddingEndOfCar = 25.dp
    private var isProcessTextEnable = false

    private val carDrawable: Drawable
    private val maskDrawable: Drawable
    private lateinit var maskImage: Bitmap
    private val processLayout: ProcessLayout
    private val stickLayout: StickLayout
    private val textLayout: TextLayout

    init {
        carDrawable = AppCompatResources.getDrawable(context, R.drawable.charging_bg_car)
            ?: throw Resources.NotFoundException("charging_bg_car")
        maskDrawable = AppCompatResources.getDrawable(context, R.drawable.charging_car_mask)
            ?: throw Resources.NotFoundException("charging_car_mask")
        paintMask.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
        processLayout = ProcessLayout(
            getContext(),
            paddingStartOfCar,
            paddingEndOfCar,
            R.drawable.charging_process_green
        )
        stickLayout = StickLayout(
            getContext(),
            paddingStartOfCar,
            paddingEndOfCar,
            R.drawable.charging_stick,
        ) {
            invalidate()
        }
        textLayout = TextLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        widthDp = wSpecSize
        heightDp = hSpecSize
        carDrawable.setBounds(0, 0, widthDp, heightDp)
        maskDrawable.setBounds(0, 0, widthDp, heightDp)
        maskImage = maskDrawable.toBitmap(widthDp, heightDp)
        processLayout.onMeasure(widthDp, heightDp)
        stickLayout.onMeasure(widthDp, heightDp)
        textLayout.onMeasure(widthDp, heightDp)
    }

    fun setProcessTextEnable(isEnable: Boolean) {
        this.isProcessTextEnable = isEnable
        invalidate()
    }

    fun setCharging(isCharging: Boolean) {
        stickLayout.startCharging(isCharging)
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
        textLayout.onDraw(canvas, process, isProcessTextEnable)

        canvas.drawBitmap(maskImage, 0f, 0f, paintMask)
        canvas.restoreToCount(sc)
    }

    fun process(process: Float) {
        this.process = process
        invalidate()
        stickLayout.process(process)
    }

    override fun onDetachedFromWindow() {
        stickLayout.release()
        super.onDetachedFromWindow()
    }
}