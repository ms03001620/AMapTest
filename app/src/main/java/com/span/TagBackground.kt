package com.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt

class TagBackground(
    val bgColor: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
) {
    private val bgPaint = Paint()

    private var width = 0.0f
    private var tagWidth = 0.0f
    val rect = Rect()

    fun getSize(width: Float, tagWidth:Float){
        this.tagWidth = tagWidth
        bgPaint.setColor(bgColor)
    }

     fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
         rect.left = x.roundToInt()
         rect.top = top
         rect.right = (x+tagWidth).roundToInt()
         rect.bottom = top + (bottom-top)

         canvas.drawRect(rect, bgPaint)
    }


}