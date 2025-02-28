package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class DrawPathRectView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // Data
    private var data: MutableList<MutableList<Point>> = mutableListOf()
    private var maxPointsPerShape: Int = 20
    private var maxShapes: Int = 4
    private var callback: ((String) -> Unit)? = null
    private var scaleSize: Point = Point(100, 100)

    // State
    private var isEditable: Boolean = false
    private var isAppendMode: Boolean = false
    var currentShapeIndex: Int = 0

    // Paints
    private val redPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val greenPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Methods
    fun setData(
        data: MutableList<MutableList<Point>>,
        maxPointsPerShape: Int,
        maxShapes: Int,
        callback: (String) -> Unit,
        scaleSize: Point
    ) {
        this.data = data
        this.maxPointsPerShape = maxPointsPerShape
        this.maxShapes = maxShapes
        this.callback = callback
        this.scaleSize = scaleSize
        invalidate()
    }

    fun enableEdit(enable: Boolean) {
        isEditable = enable
        invalidate()
    }

    fun switchTo(index: Int) {
        if (isEditable) {
            if (index in 0 until maxShapes) {
                currentShapeIndex = index
                invalidate()
            }
        }
    }

    fun clear() {
        if (isEditable) {
            data[currentShapeIndex].clear()
            invalidate()
        }
    }

    fun finishAppend() {
        isAppendMode = false
        invalidate()
    }

    // Drawing
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((index, shape) in data.withIndex()) {
            drawShape(canvas, shape, index == currentShapeIndex)
        }
    }

    private fun drawShape(canvas: Canvas, shape: List<Point>, isCurrentShape: Boolean) {
        if (shape.isEmpty()) return
        val path = Path()
        val firstPoint = mapToScreenPoint(shape[0])
        path.moveTo(firstPoint.x.toFloat(), firstPoint.y.toFloat())
        for (i in 1 until shape.size) {
            val point = mapToScreenPoint(shape[i])
            path.lineTo(point.x.toFloat(), point.y.toFloat())
        }

        if (isCurrentShape) {
            if (!isAppendMode) {
                // 当前图形追加模式时不自动闭合
                path.close()
            }
        } else {
            path.close()
        }

        canvas.drawPath(path, if (isCurrentShape) redPaint else greenPaint)
    }

    // Touch Events
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditable) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isAppendMode = true
                val startPoint = Point(event.x.roundToInt(), event.y.roundToInt())
                addDownPoint(startPoint)
                return true
            }

            MotionEvent.ACTION_UP -> {
                val endPoint = Point(event.x.roundToInt(), event.y.roundToInt())
                addUpPoint(endPoint)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addUpPoint(point: Point) {
        val currentShape = data[currentShapeIndex]
        if (currentShape.size >= maxPointsPerShape) {
            callback?.invoke("Max points reached")
            return
        }
        currentShape.add(mapToScalePoint(point))
        invalidate()
    }

    private fun addDownPoint(point: Point) {
        val currentShape = data[currentShapeIndex]
        if (currentShape.size == 0) {
            currentShape.add(mapToScalePoint(point))
            invalidate()
        }
    }

    // Mapping
    private fun mapToScreenPoint(point: Point): Point {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        val scaleWidth = scaleSize.x.toFloat()
        val scaleHeight = scaleSize.y.toFloat()

        val x = (point.x / scaleWidth) * screenWidth
        val y = (point.y / scaleHeight) * screenHeight
        return Point(x.roundToInt(), y.roundToInt())
    }

    private fun mapToScalePoint(point: Point): Point {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        val scaleWidth = scaleSize.x.toFloat()
        val scaleHeight = scaleSize.y.toFloat()

        val x = ((point.x / screenWidth) * scaleWidth).roundToInt()
        val y = ((point.y / screenHeight) * scaleHeight).roundToInt()
        return Point(x, y)
    }
}