package com.example.amaptest.rect

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import kotlin.math.max
import kotlin.math.min

class GridDrawView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BORDER_WIDTH = 4
        private const val DEFAULT_HORIZONTAL_CELL_COUNT = 22
        private const val DEFAULT_VERTICAL_CELL_COUNT = 18
        private const val DEFAULT_CELL_SPACING = -2
    }

    private val borderPaint: Paint
    private val activeBorderPaint: Paint
    private val rectPaint: Paint
    private var borderWidth: Float
    private var horizontalCellCount: Int
    private var verticalCellCount: Int
    private var cellSpacing: Float

    private var drawRectangle = false
    private var beginCoordinate: PointF = PointF()
    private var endCoordinate: PointF = PointF()

    private val activatedCells: MutableSet<Pair<Int, Int>> = mutableSetOf()

    init {
        borderWidth = DEFAULT_BORDER_WIDTH.toFloat()
        horizontalCellCount = DEFAULT_HORIZONTAL_CELL_COUNT
        verticalCellCount = DEFAULT_VERTICAL_CELL_COUNT
        cellSpacing = DEFAULT_CELL_SPACING.toFloat()

        borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = Color.TRANSPARENT
            isAntiAlias = true
        }

        activeBorderPaint = Paint(borderPaint).apply {
            color = Color.RED
        }

        rectPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = Color.RED
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw grid
        drawGrid(canvas)

        // Draw rectangle
        if (drawRectangle) {
            canvas.drawRect(
                beginCoordinate.x,
                beginCoordinate.y,
                endCoordinate.x,
                endCoordinate.y,
                rectPaint
            )
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val cellWidth = CalcHelper.getCellWidth(width, horizontalCellCount, cellSpacing)
        val cellHeight = CalcHelper.getCellHeight(height, verticalCellCount, cellSpacing)

        for (i in 0 until verticalCellCount) {
            for (j in 0 until horizontalCellCount) {
                val (left, top, right, bottom) = CalcHelper.getCellRect(j, i, cellWidth, cellHeight, cellSpacing)
                val (innerLeft, innerTop, innerRight, innerBottom) = CalcHelper.getInnerCellRect(left, top, right, bottom, borderWidth)

                val paint = if (activatedCells.contains(Pair(i, j))) {
                    activeBorderPaint
                } else {
                    borderPaint
                }
                canvas.drawRect(innerLeft, innerTop, innerRight, innerBottom, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawRectangle = true
                beginCoordinate.x = event.x
                beginCoordinate.y = event.y
                endCoordinate.x = event.x
                endCoordinate.y = event.y
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                endCoordinate.x = event.x
                endCoordinate.y = event.y
                invalidate()
            }

            MotionEvent.ACTION_UP -> {
                drawRectangle = false
                val rect = Rect(
                    min(beginCoordinate.x.toInt(), endCoordinate.x.toInt()),
                    min(beginCoordinate.y.toInt(), endCoordinate.y.toInt()),
                    max(beginCoordinate.x.toInt(), endCoordinate.x.toInt()),
                    max(beginCoordinate.y.toInt(), endCoordinate.y.toInt())
                )
                checkIntersections(rect)
                invalidate()
            }
        }
        return true
    }

    private fun checkIntersections(rect: Rect) {
        val cellWidth = CalcHelper.getCellWidth(width, horizontalCellCount, cellSpacing)
        val cellHeight = CalcHelper.getCellHeight(height, verticalCellCount, cellSpacing)

        for (i in 0 until verticalCellCount) {
            for (j in 0 until horizontalCellCount) {
                val (left, top, right, bottom) = CalcHelper.getCellRect(j, i, cellWidth, cellHeight, cellSpacing)
                val cellRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                if (Rect.intersects(rect, cellRect)) {
                    activatedCells.add(Pair(i, j))
                }
            }
        }
    }

    fun deactivateAllCells() {
        activatedCells.clear()
        invalidate()
    }

    fun getActivatedCellsArray(): List<List<Int>> {
        return List(verticalCellCount) { i ->
            List(horizontalCellCount) { j ->
                if (activatedCells.contains(Pair(i, j))) 1 else 0
            }
        }
    }

    fun initializeCells(array: List<List<Int>>) {
        if (array.size != verticalCellCount || array.firstOrNull()?.size != horizontalCellCount) {
            throw IllegalArgumentException("Invalid array dimensions")
        }
        activatedCells.clear()
        for (i in 0 until verticalCellCount) {
            for (j in 0 until horizontalCellCount) {
                if (array[i][j] == 1) {
                    activatedCells.add(Pair(i, j))
                }
            }
        }
        invalidate()
    }

    object CalcHelper {
        fun getCellWidth(totalWidth: Int, horizontalCellCount: Int, cellSpacing: Float): Float {
            return (totalWidth - (horizontalCellCount + 1) * cellSpacing) / horizontalCellCount.toFloat()
        }

        fun getCellHeight(totalHeight: Int, verticalCellCount: Int, cellSpacing: Float): Float {
            return (totalHeight - (verticalCellCount + 1) * cellSpacing) / verticalCellCount.toFloat()
        }

        fun getCellRect(j: Int, i: Int, cellWidth: Float, cellHeight: Float, cellSpacing: Float): RectF {
            val left = j * (cellWidth + cellSpacing) + cellSpacing
            val top = i * (cellHeight + cellSpacing) + cellSpacing
            val right = left + cellWidth
            val bottom = top + cellHeight
            return RectF(left, top, right, bottom)
        }

        fun getInnerCellRect(left: Float, top: Float, right: Float, bottom: Float, borderWidth: Float): RectF {
            val innerBorderOffset = borderWidth / 2
            val innerLeft = left + innerBorderOffset
            val innerTop = top + innerBorderOffset
            val innerRight = right - innerBorderOffset
            val innerBottom = bottom - innerBorderOffset
            return RectF(innerLeft, innerTop, innerRight, innerBottom)
        }
    }

    data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)
}