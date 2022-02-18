package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.ColorInt
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge

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

    fun makeIcon(text: String): Bitmap? {
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
                loge("makeIconCluster", "logicException", e)
                logd("text: $text", "logicException")
                return null
            }
        }
        return bitmapCache
    }
}