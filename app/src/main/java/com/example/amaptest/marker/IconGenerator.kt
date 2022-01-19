package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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

    private val sizeSingle = lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        container.measure(measureSpec, measureSpec)
        val measuredWidth: Int = container.getMeasuredWidth()
        val measuredHeight: Int = container.getMeasuredHeight()
        container.layout(0, 0, measuredWidth, measuredHeight)
        Pair(measuredWidth, measuredHeight)
    }

    private val sizeCluster = lazy {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        containerCluster.measure(measureSpec, measureSpec)
        val measuredWidth: Int = containerCluster.getMeasuredWidth()
        val measuredHeight: Int = containerCluster.getMeasuredHeight()
        containerCluster.layout(0, 0, measuredWidth, measuredHeight)
        Pair(measuredWidth, measuredHeight)
    }

    fun makeIcon(text: CharSequence): Bitmap {
        return makeIconCluster(text, textView, container, sizeSingle.value)
    }

    fun makeIconCluster(text: CharSequence): Bitmap {
        return makeIconCluster(text, textViewCluster, containerCluster, sizeCluster.value)
    }

    private fun makeIconCluster(
        text: CharSequence,
        textView: TextView,
        container: View,
        size: Pair<Int, Int>
    ): Bitmap {
        textView.text = text
        val bitmap = Bitmap.createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        container.draw(canvas)
        return bitmap
    }

}