package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ClickableViewAccessibility")
class ScreenRectView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var targetWidth = 200
    var targetHeight = 200
    var strokeWidth = 4f
        set(value) {
            field = value
            borderPaint.strokeWidth = value
            invalidate()
        }
    private var initialRectList: List<Int>? = null

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var rect: Rect? = null
    private var resultListener: ((x: Int, y: Int, w: Int, h: Int) -> Unit)? = null
    private var isDrawing = false
    private var enableDraw = false
    var resultList: List<Int>? = null
        private set

    private val borderPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = this@ScreenRectView.strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rect?.let {
            canvas.drawRect(it, borderPaint)
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enableDraw) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDrawing = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    endX = event.x
                    endY = event.y
                    updateRect()
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isDrawing) {
                    endX = event.x
                    endY = event.y
                    updateRect()
                    calculateResult()
                    invalidate()
                    isDrawing = false
                    return true
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDrawing) {
                    isDrawing = false
                    calculateResult()
                    return true
                }
            }
        }
        return false
    }

    private fun updateRect() {
        val left = min(startX, endX)
        val top = min(startY, endY)
        val right = max(startX, endX)
        val bottom = max(startY, endY)

        val clampedLeft = max(0f, left)
        val clampedTop = max(0f, top)
        val clampedRight = min(width.toFloat(), right)
        val clampedBottom = min(height.toFloat(), bottom)

        rect = Rect(clampedLeft.toInt(), clampedTop.toInt(), clampedRight.toInt(), clampedBottom.toInt())
    }

    private fun calculateResult() {
        rect?.let {
            val screenWidth = width.toFloat()
            val screenHeight = height.toFloat()

            val rectWidth = it.width().toFloat()
            val rectHeight = it.height().toFloat()

            val xRatio = it.left / screenWidth
            val yRatio = it.top / screenHeight
            val wRatio = rectWidth / screenWidth
            val hRatio = rectHeight / screenHeight

            val resultX = (xRatio * targetWidth).toInt()
            val resultY = (yRatio * targetHeight).toInt()
            val resultW = (wRatio * targetWidth).toInt()
            val resultH = (hRatio * targetHeight).toInt()

            resultListener?.invoke(resultX, resultY, resultW, resultH)
            resultList = listOf(resultX, resultY, resultW, resultH)
        }
    }

    fun setResultListener(listener: (x: Int, y: Int, w: Int, h: Int) -> Unit) {
        resultListener = listener
    }

    fun clear() {
        rect = null
        resultList = null
        invalidate()
    }

    fun setEnableDraw(enable: Boolean) {
        enableDraw = enable
    }

    fun setInitialRectList(list: List<Int>?) {
        initialRectList = list
        post {
            drawInitialRect()
        }
    }

    private fun drawInitialRect() {
        initialRectList?.let { list ->
            if (list.size == 4) {
                val x = list[0]
                val y = list[1]
                val w = list[2]
                val h = list[3]

                val screenWidth = width.toFloat()
                val screenHeight = height.toFloat()

                val left = (x.toFloat() / targetWidth) * screenWidth
                val top = (y.toFloat() / targetHeight) * screenHeight
                val right = ((x + w).toFloat() / targetWidth) * screenWidth
                val bottom = ((y + h).toFloat() / targetHeight) * screenHeight

                rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                calculateResult()
                invalidate()
            }
        }
    }
}