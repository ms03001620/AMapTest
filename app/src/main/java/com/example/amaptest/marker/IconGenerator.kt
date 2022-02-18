package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.LruCache
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import com.polestar.base.utils.loge
import com.polestar.base.utils.logw

class IconGenerator(
    val context: Context,
    resId: Int,
    @ColorInt textColor: Int,
    private val offsetHeight4Text: Int = 0
) {
    private val container =
        LayoutInflater.from(context).inflate(resId, null)

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

    private var bitmapCache: Bitmap? = null
    private var text: String = ""

    private val cache = object : LruCache<String, Bitmap>(512) {
        override fun sizeOf(key: String?, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun makeIcon(text: String): Bitmap? {
        if (ENABLE_CACHE) {
            return createIconByCache(text)
        } else {
            return createIcon(text)
        }
    }

    private fun createIconByCache(text: String): Bitmap? {
        val bitmap = cache.get(text)

        if (bitmap != null) {
            return bitmap
        } else {
            val newBitmap = createIcon(text)
            if (newBitmap != null) {
                cache.put(text, newBitmap)
            }
            return newBitmap
        }
    }

    private fun createIcon(text: String): Bitmap? {
        if (this.text == text && bitmapCache != null) {
            logw("makeIcon same:$text", TAG)
            assert(!ENABLE_CACHE)
        }
        this.text = text

        if (bitmapCache == null) {
            bitmapCache =
                Bitmap.createBitmap(
                    sizeSingle.first.width,
                    sizeSingle.first.height,
                    Bitmap.Config.ARGB_8888
                )
        }
        return makeIconCluster(text)
    }

    val paint = Paint().also {
        it.color = textColor
        it.textSize = 50f
        it.textAlign = Paint.Align.CENTER
    }

    private fun makeIconCluster(text: String): Bitmap? {
        bitmapCache?.let {
            try {
                it.eraseColor(Color.TRANSPARENT)
                val c = Canvas(it)

                c.drawBitmap(sizeSingle.second, 0f, 0f, paint)

                val xPos = it.width / 2.0f
                val yPos = it.height / 2.0f - ((paint.descent() + paint.ascent()) / 2)

                c.drawText(text, xPos, yPos + offsetHeight4Text, paint)

            } catch (e: Exception) {
                loge("makeIconCluster", TAG, e)
                loge("makeIconCluster", "logicException", e)
                return null
            }
        }
        return bitmapCache
    }

    companion object {
        const val ENABLE_CACHE = true
        const val TAG = "IconGenerator"
    }
}