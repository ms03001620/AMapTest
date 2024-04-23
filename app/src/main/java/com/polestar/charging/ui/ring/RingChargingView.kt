package com.polestar.charging.ui.ring

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View


class RingChargingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var process: Float = 0.0f
    private var isProcessTextEnable = false

    private val processLayout = ProcessLayout()
    private val textLayout = TextLayout()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthDp = wSpecSize
        val heightDp = hSpecSize
        processLayout.onMeasure(widthDp, heightDp)
        textLayout.onMeasure(widthDp, heightDp)
    }

    fun setProcessTextEnable(isEnable: Boolean) {
        this.isProcessTextEnable = isEnable
        invalidate()
    }

    fun setCharging(isCharging: Boolean) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        processLayout.onDraw(canvas, process)
        textLayout.onDraw(canvas, process, isProcessTextEnable)
    }

    fun process(process: Float) {
        this.process = process
        invalidate()
    }
}