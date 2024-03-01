package com.example.amaptest

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull

@SuppressLint("AppCompatCustomView")
class MaskFrameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var widthDp = 0
    private var heightDp = 0
    private var stickWidth = 40

    private val carDrawable: Drawable
    private val processDrawable: Drawable
    private val maskDrawable: Drawable
    private val stickDrawable: Drawable

    private var paintMask = Paint()

    private var animator: ValueAnimator? = null

    init {
        carDrawable= getContext().getDrawable(R.drawable.charging_bg_car)!!
        processDrawable= getContext().getDrawable(R.drawable.bg_car_progress)!!
        maskDrawable= getContext().getDrawable(R.drawable.charging_bg_car_single)!!
        stickDrawable= getContext().getDrawable(R.drawable.bg_working_single)!!

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
        processDrawable.setBounds(0, 0, widthDp/2, heightDp)
        maskDrawable.setBounds(0, 0, widthDp/2, heightDp)
        stickDrawable.setBounds(0, 0, stickWidth, heightDp)
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

        processDrawable.draw(canvas)
        stickDrawable.draw(canvas)
        canvas.drawBitmap(maskDrawable.toBitmap(widthDp, heightDp), 0f, 0f, paintMask)
        canvas.restoreToCount(sc)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        startAnimation()
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, widthDp).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 2000
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int

                stickDrawable.setBounds(value, 0, (stickWidth)+value, heightDp)
                invalidate()
            }
        }
        animator?.start()
    }


}