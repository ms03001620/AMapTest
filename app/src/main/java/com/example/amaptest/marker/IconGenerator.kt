package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.LruCache
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import com.polestar.base.utils.logv

class IconGenerator(
    val context: Context,
    resId: Int,
    textColor: Int,
    textSizePx: Float = 50f,
    private val offsetHeight4Text: Int = 0
) {
    private val container =
        LayoutInflater.from(context).inflate(resId, null)

    private val paint = Paint().also {
        it.color = textColor
        it.textSize = textSizePx
        it.textAlign = Paint.Align.CENTER
    }

    private val sizeSingle by lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        container.measure(measureSpec, measureSpec)
        val measuredWidth: Int = container.measuredWidth
        val measuredHeight: Int = container.measuredHeight
        container.layout(0, 0, measuredWidth, measuredHeight)

        val bitmap = Bitmap.createBitmap(
            measuredWidth,
            measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        container.draw(Canvas(bitmap))

        Pair<Size, Bitmap>(Size(measuredWidth, measuredHeight), bitmap)
    }

    //2022-02-10 10:53:41.981 2790-2833/com.example.amaptest V/_____: k:2, t:63504, t1024:62
    //2022-02-10 10:53:42.085 2790-2833/com.example.amaptest V/_____: k:15, t:53340, t1024:52
    private val cache = object : LruCache<String, Bitmap>(63504 * 10) {
        override fun sizeOf(key: String?, value: Bitmap): Int {
            logv("sizeOf key:$key, values hash:${value.hashCode()}", TAG)

            return value.byteCount
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String?,
            oldValue: Bitmap?,
            newValue: Bitmap?
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)

            logv("removed key:$key", TAG)
            if (oldValue?.isRecycled?.not() == true) {
                oldValue.recycle()
            }
        }
    }

    fun makeIcon(text: String): Bitmap? {
        if (ENABLE_CACHE) {
            return makeIconBitmapOrCache(text)
        } else {
            return makeIconBitmap(text)
        }
    }

    private fun makeIconBitmapOrCache(text: String): Bitmap? {
        val bitmap = cache.get(text)

        return if (bitmap != null) {
            bitmap
        } else {
            val newBitmap = makeIconBitmap(text)
            if (newBitmap != null) {
                cache.put(text, newBitmap)
            }
            newBitmap
        }
    }

    private fun makeIconBitmap(text: String): Bitmap? {
        if (ENABLE_CACHE.not()) {
            Thread.sleep(200)
        }
        val bitmap = Bitmap.createBitmap(
            sizeSingle.first.width,
            sizeSingle.first.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        canvas.drawBitmap(sizeSingle.second, 0f, 0f, paint)

        val xPos = bitmap.width / 2.0f
        val yPos = bitmap.height / 2.0f - ((paint.descent() + paint.ascent()) / 2)

        canvas.drawText(text, xPos, yPos + offsetHeight4Text, paint)
        return bitmap
    }

    companion object {
        const val ENABLE_CACHE = true
        const val TAG = "IconGenerator"
    }
}