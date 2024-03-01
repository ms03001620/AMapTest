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
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmapOrNull

@SuppressLint("AppCompatCustomView")
class MaskFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var paintMask = Paint()
    private var bitmapMask: Bitmap? = null
    private var matrixMask = android.graphics.Matrix()
    private var animator: ValueAnimator? = null

    private val stickBar = RectF(0f, 0f, 50f, 50f)
    private var stickBarX = 0.0f


    init {
        paintMask.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
    }

    override fun dispatchDraw(canvas: Canvas) {
        background?.draw(canvas)

        val sc = canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            null,
            Canvas.ALL_SAVE_FLAG
        )

        super.dispatchDraw(canvas)

        if (bitmapMask == null) {
            val childFirst = getChildAt(0)
            if (childFirst is ImageView) {
                bitmapMask = drawableToBitmap(childFirst.drawable)
                matrixMask = childFirst.imageMatrix
            }
        }

        bitmapMask?.let { mFinalMask ->
            canvas.drawBitmap(mFinalMask, matrixMask, paintMask)
        }

        canvas.restoreToCount(sc)
    }

    private fun startAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 360).apply {
            //repeatCount = ValueAnimator.INFINITE
            duration = 2000
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                stickBarX = (valueAnimator.animatedValue as Int).toFloat()
                println("stickBarX:$stickBarX")
            }
        }
        animator?.start()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        if (drawable is GradientDrawable) {
            drawable.toBitmapOrNull()
        }
        throw UnsupportedOperationException("Unsupported: ${drawable.javaClass.simpleName}")
    }

}