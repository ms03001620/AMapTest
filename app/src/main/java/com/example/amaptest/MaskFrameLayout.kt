package com.example.amaptest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull

@SuppressLint("AppCompatCustomView")
class MaskFrameLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {


    var mPaint = Paint()
    var W = 100
    var H = 100


    /*    override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.GREEN)
            val sc = canvas.saveLayer(0f, 0f, W.toFloat(), H.toFloat(), null, Canvas.ALL_SAVE_FLAG)
            canvas.drawBitmap(makeDst(W, H), 0f, 0f, mPaint)
            mPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
            canvas.drawBitmap(makeMask(W, H), 0f, 0f, mPaint)
            mPaint.setXfermode(null)
            canvas.restoreToCount(sc)
        }
        */
    var mFinalMask: Bitmap? = null//akeMask(100, 100)

    var matrixMask = android.graphics.Matrix()

    override fun dispatchDraw(canvas: Canvas) {
        background?.draw(canvas)

        val sc =
            canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)

        super.dispatchDraw(canvas)

        val childFirst = getChildAt(0)
        if (mFinalMask == null && childFirst is ImageView) {
            mFinalMask = drawableToBitmap(childFirst.drawable)
            matrixMask = childFirst.imageMatrix
            mPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
        }

        mFinalMask?.let { mFinalMask ->
            canvas.drawBitmap(mFinalMask, matrixMask, mPaint)
        }

        canvas.restoreToCount(sc)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        if (drawable is GradientDrawable) {
            drawable.toBitmapOrNull()
        }
        throw UnsupportedOperationException()
    }


    companion object {
        fun makeDst(w: Int, h: Int): Bitmap {
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bm)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = -0x33bc
            c.drawOval(RectF(0f, 0f, (w * 3 / 4).toFloat(), (h * 3 / 4).toFloat()), p)
            return bm
        }

        // create a bitmap with a rect, used for the "src" image
        fun makeMask(w: Int, h: Int): Bitmap {
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bm)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = -0x995501
            c.drawRect(
                (w / 3).toFloat(),
                (h / 3).toFloat(),
                (w * 19 / 20).toFloat(),
                (h * 19 / 20).toFloat(),
                p
            )
            return bm
        }
    }

}