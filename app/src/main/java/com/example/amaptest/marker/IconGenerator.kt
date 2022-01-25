package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.amaptest.R

class IconGenerator(val context: Context) {
    private val container =
        LayoutInflater.from(context).inflate(R.layout.charging_layout_marker_collapsed_v2, null)

    private val containerCluster =
        LayoutInflater.from(context).inflate(R.layout.charging_layout_marker_cluster_v2, null)

    private val textView = container.findViewById<TextView>(R.id.tv)
    private val textViewCluster = containerCluster.findViewById<TextView>(R.id.text_cluster)

    private val sizeSingle by lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        container.measure(measureSpec, measureSpec)
        val measuredWidth: Int = container.measuredWidth
        val measuredHeight: Int = container.measuredHeight
        container.layout(0, 0, measuredWidth, measuredHeight)
        Size(measuredWidth, measuredHeight)
    }

    private val sizeCluster by lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        containerCluster.measure(measureSpec, measureSpec)
        val measuredWidth: Int = containerCluster.measuredWidth
        val measuredHeight: Int = containerCluster.measuredHeight
        containerCluster.layout(0, 0, measuredWidth, measuredHeight)
        Size(measuredWidth, measuredHeight)
    }

    fun makeIcon(text: CharSequence): Bitmap {
        return makeIconCluster(text, textView, container, sizeSingle)
    }

    fun makeIconCluster(text: CharSequence): Bitmap {
        return makeIconCluster(text, textViewCluster, containerCluster, sizeCluster)
    }

    private fun makeIconCluster(
        text: CharSequence,
        textView: TextView,
        container: View,
        size: Size
    ): Bitmap {
        textView.text = text
        val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        container.draw(canvas)
        return bitmap
    }

}