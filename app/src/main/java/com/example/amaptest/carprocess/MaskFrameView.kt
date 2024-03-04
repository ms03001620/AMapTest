package com.example.amaptest.carprocess

import android.animation.ValueAnimator
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
import android.view.animation.LinearInterpolator
import androidx.core.graphics.drawable.toBitmap
import com.example.amaptest.R
import com.example.amaptest.ui.main.dp
import kotlin.math.roundToInt

@SuppressLint("AppCompatCustomView")
class MaskFrameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var widthDp = 0
    private var heightDp = 0
    private var stickWidth = 60.dp
    private var process: Float = 0.0f

    private val carDrawable: Drawable
    private val processLayout: ProcessLayout


    private val maskDrawable: Drawable
    private lateinit var maskImage: Bitmap
    private val stickDrawable: Drawable

    private var paintMask = Paint()
    private var animator: ValueAnimator? = null

    private var paddingStartOfCar = 35.dp
    private var paddingEndOfCar = 25.dp

    init {
        carDrawable = getContext().getDrawable(R.drawable.charging_bg_car)!!
        processLayout = ProcessLayout(getContext(), paddingStartOfCar, paddingEndOfCar)
        maskDrawable = getContext().getDrawable(R.drawable.charging_bg_car_single)!!
        stickDrawable = getContext().getDrawable(R.drawable.bg_working)!!
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
        stickDrawable.setBounds(0, 0, stickWidth, heightDp)
        maskImage = maskDrawable.toBitmap(widthDp, heightDp)

        processLayout.onMeasure(widthDp, heightDp)
    }



    private fun getProcessWith() = (widthDp * process).roundToInt()

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

        if (process > 0) {
            stickDrawable.draw(canvas)
        }

        canvas.drawBitmap(maskImage, 0f, 0f, paintMask)
        canvas.restoreToCount(sc)
    }

    private fun startAnimation() {
        animator?.cancel()
        val w = getProcessWith()
        if (w > 0) {
            animator = ValueAnimator.ofInt(0, getProcessWith()).apply {
                repeatCount = ValueAnimator.INFINITE
                duration = 2000
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val start = value - stickWidth

                    stickDrawable.setBounds(start, 0, value, heightDp)
                    invalidate()
                }
            }
            animator?.start()
        }
    }

    fun process(process: Float) {
        this.process = process
        invalidate()

        startAnimation()
    }


}