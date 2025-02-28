package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class PathsRectView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var isDrawing = false


    private val borderPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val currentPath = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        //将坐标原点移动到（300,300,）
        //currentPath.moveTo(300f, 300f);
        //连接(300, 300)和(300, 600)成一条线
        //currentPath.lineTo(300f, 600f);
        //连接(300, 600)和(600, 600)成一条线
        //currentPath.lineTo(600f, 600f);
        //path.close();暂时注释

        canvas.drawPath(currentPath, borderPaint);
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDrawing = true
                currentPath.moveTo(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {

                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDrawing) {
                    currentPath.lineTo(event.x, event.y);
                   // currentPath.close()
                    invalidate()
                    isDrawing = false
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDrawing) {
                    isDrawing = false
                    return true
                }
            }
        }
        return false
    }



}