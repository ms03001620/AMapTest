package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class TagBackground(
    val bgColor: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
) {
    private val bgRectF = RectF()
    private val bgPaint = Paint()

    private var width = 0.0f
    private var tagWidth = 0.0f

    fun getSize(width: Float, tagWidth:Float){
        this.width = width
        this.tagWidth = tagWidth
        bgPaint.setColor(bgColor)
    }

    fun drawBg(
        canvas: Canvas,
        top: Int,
        bottom: Int,
    ) {
        bgRectF.left = width - tagWidth
        bgRectF.top = top.toFloat()
        bgRectF.right = width
        bgRectF.bottom = bottom.toFloat()

        canvas.drawRect(
            bgRectF.left,
            bgRectF.top + paddingTop,
            bgRectF.right,
            bgRectF.bottom + paddingBottom,
            bgPaint
        )
    }

    fun getBgRect() = bgRectF
}