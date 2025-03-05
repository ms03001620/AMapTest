package com.example.amaptest.ui.main

import android.graphics.Point
import android.graphics.Rect

object ScaleScreenCalc {

    fun mapScaleToScreen(
        scaleRect: Rect,
        targetSize: Point,
        canvasSize: Point
    ): Rect {
        val targetRatioX =  canvasSize.x / targetSize.x.toFloat()
        val targetRatioY =  canvasSize.y / targetSize.y.toFloat()

        val left = scaleRect.left * targetRatioX
        val top = scaleRect.top * targetRatioY
        val right = scaleRect.right * targetRatioX
        val bottom = scaleRect.bottom * targetRatioY

        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    fun mapScreenToScale(
        screen: Rect,
        targetSize: Point,
        canvasSize: Point,
    ): Rect {
        val targetRatioX =  canvasSize.x / targetSize.x.toFloat()
        val targetRatioY =  canvasSize.y / targetSize.y.toFloat()


        val x = screen.left / targetRatioX
        val y = screen.top / targetRatioY
        val w = screen.width() / targetRatioX
        val h = screen.height() / targetRatioY

        return Rect(x.toInt(), y.toInt(), w.toInt(), h.toInt())
    }
}