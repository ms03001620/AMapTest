package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

class IconGenerator(val context: Context, resId: Int, textResId: Int) {
    private val container =
        LayoutInflater.from(context).inflate(resId, null)

    private val textView = container.findViewById<TextView>(textResId)

    private val sizeSingle by lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        container.measure(measureSpec, measureSpec)
        val measuredWidth: Int = container.measuredWidth
        val measuredHeight: Int = container.measuredHeight
        container.layout(0, 0, measuredWidth, measuredHeight)
        Size(measuredWidth, measuredHeight)
    }

    private var bitmapCache: Bitmap? = null

    fun makeIcon(text: CharSequence): Bitmap {
        if (bitmapCache == null) {
            bitmapCache =
                Bitmap.createBitmap(sizeSingle.width, sizeSingle.height, Bitmap.Config.ARGB_8888)
        }
        return makeIconCluster(text, container, bitmapCache!!)
    }

    private fun makeIconCluster(
        text: CharSequence,
        container: View,
        bitmap: Bitmap
    ): Bitmap {
        textView.text = text
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        container.draw(canvas)
        return bitmap
    }

}