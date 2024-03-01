package com.example.amaptest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView

@SuppressLint("AppCompatCustomView")
class MaskFrameLayout(context: Context, attrs: AttributeSet?) :FrameLayout(context, attrs) {


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
    var mFinalMask: Bitmap?= makeMask(100, 100)

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        val childFirst = getChildAt(0)
        if(childFirst is ImageView){
            val drawable = childFirst.getDrawable() as BitmapDrawable

            mFinalMask = drawable.getBitmap()
        }


        if (mFinalMask != null) {
            mPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))
            canvas.drawBitmap(mFinalMask!!, 0.0f, 0.0f, mPaint)
            mPaint.setXfermode(null)
        }
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