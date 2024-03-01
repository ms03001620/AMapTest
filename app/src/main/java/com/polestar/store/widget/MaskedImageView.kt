package com.polestar.store.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView

@SuppressLint("AppCompatCustomView")
class MaskedImageView : ImageView {
    private val srcBitmap: Bitmap? = null
    private val foregroundDrawable: Drawable? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    var mPaint = Paint()
    var W = 100
    var H = 100


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.GREEN)
        val sc = canvas.saveLayer(0f, 0f, W.toFloat(), H.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        canvas.drawBitmap(makeDst(W, H), 0f, 0f, mPaint)
        mPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(makeMask(W, H), 0f, 0f, mPaint)
        mPaint.setXfermode(null)
        canvas.restoreToCount(sc)
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