package com.example.amaptest

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.graphics.drawable.NinePatchDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.updateBounds
import com.example.amaptest.ui.main.dp
import kotlin.math.roundToInt

@SuppressLint("AppCompatCustomView")
class MaskFrameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var widthDp = 0
    private var heightDp = 0
    private var stickWidth = 60.dp
    private var process: Float = 0.0f

    private val carDrawable: Drawable
    private val processDrawable: Drawable
    private lateinit var processImage: Bitmap
    private var processPaint: Paint
    private var processWidth = 0

    private val maskDrawable: Drawable
    private lateinit var maskImage: Bitmap
    private val stickDrawable: Drawable

    private var paintMask = Paint()

    private var animator: ValueAnimator? = null

    init {
        carDrawable = getContext().getDrawable(R.drawable.charging_bg_car)!!
        processDrawable = getContext().getDrawable(R.drawable.bg_green_gradient)!!
        processPaint = Paint()
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
        processDrawable.setBounds(0, 0, widthDp, heightDp)
        stickDrawable.setBounds(0, 0, stickWidth, heightDp)

        maskImage = maskDrawable.toBitmap(widthDp, heightDp)
        processWidth = widthDp
        processImage = processDrawable.toBitmap(processWidth, heightDp)
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

        if (process > 0) {
            canvas.drawBitmap(
                processImage,
                getProcessWith().toFloat() - processWidth,
                0f,
                processPaint
            )
            stickDrawable.draw(canvas)
        }

        //canvas.drawBitmap(maskImage, 0f, 0f, paintMask)
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