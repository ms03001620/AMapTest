package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * 一个自定义的MapView，支持异步设置地图尺寸、网格绘制、手势平移和缩放。
 *
 * @JvmOverloads constructor, so it can be used in XML.
 */
class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 1. 定义画笔和变换矩阵
    private val gridPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val mapMatrix = Matrix()
    private val savedMatrix = Matrix() // 用于保存触摸事件开始时的矩阵状态

    // 2. 地图尺寸参数 (要求1: 接受外部异步传入)
    /**
     * 地图的逻辑宽度。
     * 当这个值被外部设置时，会触发视图重绘。
     */
    var mapWidth: Float = 1000f
        set(value) {
            field = value
            invalidate() // 宽度变化时重绘
        }

    /**
     * 地图的逻辑高度。
     * 当这个值被外部设置时，会触发视图重绘。
     */
    var mapHeight: Float = 1000f
        set(value) {
            field = value
            invalidate() // 高度变化时重绘
        }

    // 3. 手势处理相关变量
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var scaleGestureDetector: ScaleGestureDetector

    // 定义缩放的最小和最大级别
    private val minScale = 0.5f
    private val maxScale = 3.0f
    private var currentScale = 1.0f

    // 初始化代码块
    init {
        // 初始化缩放手势检测器 (要求4: 支持手势缩放)
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }


    /**
     * 核心绘制方法 (要求2: 绘制网格)
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 保存当前画布状态
        canvas.save()
        // 将矩阵变换应用到画布上，实现平移和缩放
        canvas.concat(mapMatrix)

        // 绘制10x10的网格
        drawGrid(canvas)

        // 恢复画布到save()之前的状态
        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        if (mapWidth <= 0 || mapHeight <= 0) return

        val cellWidth = mapWidth / 10
        val cellHeight = mapHeight / 10

        // 绘制11条垂直线
        for (i in 0..10) {
            val x = i * cellWidth
            canvas.drawLine(x, 0f, x, mapHeight, gridPaint)
        }

        // 绘制11条水平线
        for (i in 0..10) {
            val y = i * cellHeight
            canvas.drawLine(0f, y, mapWidth, y, gridPaint)
        }
    }


    /**
     * 触摸事件处理 (要求3: 平移 和 要求4: 缩放)
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 将触摸事件传递给缩放手势检测器
        scaleGestureDetector.onTouchEvent(event)

        val action = event.actionMasked
        val x = event.x
        val y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                // 保存当前矩阵状态和触摸点
                savedMatrix.set(mapMatrix)
                lastTouchX = x
                lastTouchY = y
            }

            MotionEvent.ACTION_MOVE -> {
                // 如果不是在缩放状态，则执行平移
                if (!scaleGestureDetector.isInProgress) {
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    // 在之前保存的矩阵基础上进行平移
                    mapMatrix.set(savedMatrix)
                    mapMatrix.postTranslate(dx, dy)
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 手指抬起，无需操作
            }
        }
        return true // 返回true表示我们已经处理了此事件
    }

    /**
     * 内部类：处理缩放手势的回调
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            // 缩放开始时，保存当前矩阵
            savedMatrix.set(mapMatrix)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val values = FloatArray(9)
            mapMatrix.getValues(values)
            val currentScaleX = values[Matrix.MSCALE_X]

            // 计算新的缩放比例
            var newScale = currentScaleX * scaleFactor
            // 限制缩放范围
            newScale = max(minScale, min(newScale, maxScale))

            // 计算实际要应用的缩放因子
            val actualScale = newScale / currentScaleX
            currentScale = newScale // 更新当前缩放比例

            // 在之前保存的矩阵基础上，围绕手势中心点进行缩放
            mapMatrix.set(savedMatrix)
            mapMatrix.postScale(actualScale, actualScale, detector.focusX, detector.focusY)
            invalidate()
            return true
        }
    }
}