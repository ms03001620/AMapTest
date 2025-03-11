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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@SuppressLint("ClickableViewAccessibility")
class AuxiliaryLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // State
    data class Graph(
        // 主线起点
        var startPoint: Point? = null,
        // 主线终点
        var endPoint: Point? = null,
        // 辅助线起点
        var aPoint: Point? = null,
        // 辅助线终点
        var bPoint: Point? = null,
        // 主线其他点
        var paths: List<Point>? = null,
    )

    private val graphList = mutableListOf(Graph())
    private var graphIndex = 0
    private var isEditModel = false

    // Configuration
    var mainLineColor = Color.RED
    var auxiliaryLineColor = Color.YELLOW
    var textColor = Color.GREEN
    var padding = 20
    var lineWidth = 4f
    var screenSize = Point(704, 576) // Default screen size
    var arrowLength = 28

    // Paint objects
    private val mainLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mainLineColor
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
    }

    private val mainLineOffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
    }

    private val auxiliaryLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = auxiliaryLineColor
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = 40f // Adjust as needed
        textAlign = Paint.Align.CENTER
    }
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = auxiliaryLineColor
        strokeWidth = lineWidth
        style = Paint.Style.STROKE
    }

    fun setGraphNumber(number: Int) {
        if (number > 1) {
            graphList.clear()
            repeat(number) {
                graphList.add(Graph())
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        graphList.forEachIndexed { index, graph ->
            val mainLinePaint =
                if (index == graphIndex && isEditModel) mainLinePaint else mainLineOffPaint

            drawMainLine(canvas, graph, mainLinePaint)
            drawAuxiliaryLine(canvas, graph)
            drawPath(canvas, graph, mainLinePaint)
        }
    }

    private fun drawMainLine(canvas: Canvas, graph: Graph, paint: Paint) {
        val start = mapToScreenPoint(graph.startPoint ?: return)
        val end = mapToScreenPoint(graph.endPoint ?: return)

        // Draw main line
        canvas.drawLine(
            start.x.toFloat(),
            start.y.toFloat(),
            end.x.toFloat(),
            end.y.toFloat(),
            paint
        )
        // Draw "Result" label at the start point
        drawLabel(canvas, start, "Result")
    }

    private fun drawAuxiliaryLine(canvas: Canvas, graph: Graph) {
        val ap = mapToScreenPoint(graph.aPoint ?: return)
        val bp = mapToScreenPoint(graph.bPoint ?: return)

        // Draw auxiliary line and labels
        canvas.drawLine(
            ap.x.toFloat(),
            ap.y.toFloat(),
            bp.x.toFloat(),
            bp.y.toFloat(),
            auxiliaryLinePaint
        )
        // Draw arrow at B point
        drawArrow(canvas, bp, ap, arrowLength)
        // Draw A and B labels
        drawLabel(canvas, ap, "A")
        drawLabel(canvas, bp, "B")
    }

    private fun drawPath(canvas: Canvas, graph: Graph, paint: Paint) {
        val paths = graph.paths
        if (!paths.isNullOrEmpty()) {
            val end = mapToScreenPoint(graph.endPoint ?: return)
            val path = Path()
            path.moveTo(end.x.toFloat(), end.y.toFloat())
            paths.forEach {
                val point = mapToScreenPoint(it)
                path.lineTo(point.x.toFloat(), point.y.toFloat())
            }
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditModel) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                graphList[graphIndex].startPoint =
                    mapToScalePoint(Point(event.x.roundToInt(), event.y.roundToInt()))
                graphList[graphIndex].aPoint = null
                graphList[graphIndex].bPoint = null
                graphList[graphIndex].paths = null
                invalidate() // Redraw
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                graphList[graphIndex].endPoint =
                    mapToScalePoint(Point(event.x.roundToInt(), event.y.roundToInt()))
                graphList[graphIndex].aPoint = null
                graphList[graphIndex].bPoint = null
                graphList[graphIndex].paths = null
                invalidate() // Redraw
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                graphList[graphIndex].endPoint = mapToScalePoint(
                    Point(
                        max(0, min(width, event.x.roundToInt())),
                        max(0, min(height, event.y.roundToInt())),
                    )
                )

                generateAuxiliaryLine(
                    graphList[graphIndex].startPoint,
                    graphList[graphIndex].endPoint
                )?.let {
                    graphList[graphIndex].aPoint = it.first
                    graphList[graphIndex].bPoint = it.second
                }

                invalidate() // Redraw
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun generateAuxiliaryLine(
        startPoint: Point?,
        endPoint: Point?,
    ): Pair<Point, Point>? {
        if (startPoint == null || endPoint == null) {
            return null
        }

        val mainLineLength = calculateDistance(startPoint, endPoint)
        val auxiliaryLineLength = mainLineLength * 0.6
        val centerPoint = Point(
            ((startPoint.x + endPoint.x) / 2f).roundToInt(),
            ((startPoint.y + endPoint.y) / 2f).roundToInt()
        )

        // Calculate the angle of the main line
        val angle = atan2(
            (endPoint.y - startPoint.y).toDouble(),
            (endPoint.x - startPoint.x).toDouble()
        )

        // Calculate the perpendicular angle
        val perpendicularAngle = angle + Math.PI / 2

        // Calculate A and B points
        val bX = centerPoint.x + (auxiliaryLineLength / 2 * cos(perpendicularAngle)).roundToInt()
        val bY = centerPoint.y + (auxiliaryLineLength / 2 * sin(perpendicularAngle)).roundToInt()
        val aX = centerPoint.x - (auxiliaryLineLength / 2 * cos(perpendicularAngle)).roundToInt()
        val aY = centerPoint.y - (auxiliaryLineLength / 2 * sin(perpendicularAngle)).roundToInt()

        // Adjust points if they are outside the view bounds
        return adjustPointsToBounds(
            padding,
            width,
            height,
            aPoint = Point(aX, aY),
            bPoint = Point(bX, bY),
        )
    }

    private fun adjustPointsToBounds(
        padding: Int,
        width: Int,
        height: Int,
        aPoint: Point,
        bPoint: Point,
    ): Pair<Point, Point> {
        val viewBounds = android.graphics.Rect(padding, padding, width - padding, height - padding)

        // Check if A or B is out of bounds and adjust
        val aOutOfBounds = !viewBounds.contains(aPoint.x, aPoint.y)
        val bOutOfBounds = !viewBounds.contains(bPoint.x, bPoint.y)

        if (!aOutOfBounds && !bOutOfBounds) return Pair(aPoint, bPoint)

        if (aOutOfBounds && bOutOfBounds) {
            // Both points are out of bounds, move the entire line
            val dx = when {
                aPoint.x < viewBounds.left -> viewBounds.left - aPoint.x
                aPoint.x > viewBounds.right -> viewBounds.right - aPoint.x
                else -> 0
            }
            val dy = when {
                aPoint.y < viewBounds.top -> viewBounds.top - aPoint.y
                aPoint.y > viewBounds.bottom -> viewBounds.bottom - aPoint.y
                else -> 0
            }
            aPoint.offset(dx, dy)
            bPoint.offset(dx, dy)
        } else if (aOutOfBounds) {
            // Only A is out of bounds, move A towards B
            val angle = atan2((bPoint.y - aPoint.y).toDouble(), (bPoint.x - aPoint.x).toDouble())
            val moveDistance = when {
                aPoint.x < viewBounds.left -> viewBounds.left - aPoint.x
                aPoint.x > viewBounds.right -> aPoint.x - viewBounds.right
                aPoint.y < viewBounds.top -> viewBounds.top - aPoint.y
                aPoint.y > viewBounds.bottom -> aPoint.y - viewBounds.bottom
                else -> 0
            }
            val moveX = (moveDistance * cos(angle)).roundToInt()
            val moveY = (moveDistance * sin(angle)).roundToInt()
            aPoint.offset(moveX, moveY)
        } else if (bOutOfBounds) {
            // Only B is out of bounds, move B towards A
            val angle = atan2((aPoint.y - bPoint.y).toDouble(), (aPoint.x - bPoint.x).toDouble())
            val moveDistance = when {
                bPoint.x < viewBounds.left -> viewBounds.left - bPoint.x
                bPoint.x > viewBounds.right -> bPoint.x - viewBounds.right
                bPoint.y < viewBounds.top -> viewBounds.top - bPoint.y
                bPoint.y > viewBounds.bottom -> bPoint.y - viewBounds.bottom
                else -> 0
            }
            val moveX = (moveDistance * cos(angle)).roundToInt()
            val moveY = (moveDistance * sin(angle)).roundToInt()
            bPoint.offset(moveX, moveY)
        }
        // Check if after moving, the points are still out of bounds
        if (!viewBounds.contains(aPoint.x, aPoint.y) || !viewBounds.contains(bPoint.x, bPoint.y)) {
            // If still out of bounds, move the entire line
            val dx = when {
                aPoint.x < viewBounds.left -> viewBounds.left - aPoint.x
                bPoint.x < viewBounds.left -> viewBounds.left - bPoint.x
                aPoint.x > viewBounds.right -> viewBounds.right - aPoint.x
                bPoint.x > viewBounds.right -> viewBounds.right - bPoint.x
                else -> 0
            }
            val dy = when {
                aPoint.y < viewBounds.top -> viewBounds.top - aPoint.y
                bPoint.y < viewBounds.top -> viewBounds.top - bPoint.y
                aPoint.y > viewBounds.bottom -> viewBounds.bottom - aPoint.y
                bPoint.y > viewBounds.bottom -> viewBounds.bottom - bPoint.y
                else -> 0
            }
            aPoint.offset(dx, dy)
            bPoint.offset(dx, dy)
        }
        return Pair(aPoint, bPoint)
    }

    private fun drawArrow(
        canvas: Canvas,
        from: Point,
        to: Point,
        arrowLength: Int,
    ) {
        val angle = atan2((to.y - from.y).toDouble(), (to.x - from.x).toDouble())
        val arrowAngle = Math.PI / 6 // 30 degrees

        val leftArrowX = from.x + (arrowLength * cos(angle - arrowAngle)).roundToInt()
        val leftArrowY = from.y + (arrowLength * sin(angle - arrowAngle)).roundToInt()
        val rightArrowX = from.x + (arrowLength * cos(angle + arrowAngle)).roundToInt()
        val rightArrowY = from.y + (arrowLength * sin(angle + arrowAngle)).roundToInt()

        canvas.drawLine(
            from.x.toFloat(),
            from.y.toFloat(),
            leftArrowX.toFloat(),
            leftArrowY.toFloat(),
            arrowPaint
        )
        canvas.drawLine(
            from.x.toFloat(),
            from.y.toFloat(),
            rightArrowX.toFloat(),
            rightArrowY.toFloat(),
            arrowPaint
        )
    }

    private fun drawLabel(
        canvas: Canvas,
        point: Point,
        text: String,
    ) {
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        var x = point.x.toFloat()
        var y = point.y.toFloat()

        // Adjust label position to avoid overlapping with the line or going out of bounds
        if (point.x + textWidth / 2 > width - padding) {
            x = (width - padding - textWidth / 2).toFloat()
        } else if (point.x - textWidth / 2 < padding) {
            x = (padding + textWidth / 2).toFloat()
        }

        if (point.y - textHeight < padding) {
            y = (padding + textHeight).toFloat()
        } else if (point.y > height - padding) {
            y = (height - padding).toFloat()
        }

        canvas.drawText(text, x, y, textPaint)
    }

    private fun calculateDistance(p1: Point, p2: Point): Double {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx.toDouble().pow(2) + dy.toDouble().pow(2))
    }

    // Mapping
    private fun mapToScreenPoint(point: Point): Point {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        val scaleWidth = screenSize.x.toFloat()
        val scaleHeight = screenSize.y.toFloat()

        val x = (point.x / scaleWidth) * screenWidth
        val y = (point.y / scaleHeight) * screenHeight
        return Point(x.roundToInt(), y.roundToInt())
    }

    private fun mapToScalePoint(point: Point): Point {
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()
        val scaleWidth = screenSize.x.toFloat()
        val scaleHeight = screenSize.y.toFloat()

        val x = ((point.x / screenWidth) * scaleWidth).roundToInt()
        val y = ((point.y / screenHeight) * scaleHeight).roundToInt()
        return Point(x, y)
    }

    fun swapAB() {
        if (!isEditModel) return
        val ap = graphList[graphIndex].aPoint
        val bp = graphList[graphIndex].bPoint
        if (ap != null && bp != null) {
            val (x, y) = ap.x to ap.y
            graphList[graphIndex].aPoint?.x = bp.x
            graphList[graphIndex].aPoint?.y = bp.y
            graphList[graphIndex].bPoint?.x = x
            graphList[graphIndex].bPoint?.y = y
            invalidate()
        }
    }

    fun setGraph(graphs: List<List<MutableList<Int>>>) {
        graphs.forEachIndexed { index, mutableLists ->
            setCurrentGraph(index, mutableLists)
        }
    }

    fun setCurrentGraph(index: Int = 0, counterPoint: List<MutableList<Int>>) {
        if (counterPoint.size >= 4) {
            graphIndex = index
            graphList[index].aPoint = Point(counterPoint[0][0], counterPoint[0][1])
            graphList[index].bPoint = Point(counterPoint[1][0], counterPoint[1][1])

            graphList[index].startPoint = Point(counterPoint[2][0], counterPoint[2][1])
            graphList[index].endPoint = Point(counterPoint[3][0], counterPoint[3][1])
        }

        if (counterPoint.size > 4) {
            graphList[index].paths = counterPoint.subList(4, counterPoint.size).map {
                Point(it[0], it[1])
            }
        }

        invalidate()
    }

    fun getCurrentGraph(): List<List<Int>> {
        val start = graphList[graphIndex].startPoint ?: return emptyList()
        val end = graphList[graphIndex].endPoint ?: return emptyList()
        val apoint = graphList[graphIndex].aPoint ?: return emptyList()
        val bpoint = graphList[graphIndex].bPoint ?: return emptyList()

        val result = mutableListOf(
            mutableListOf(apoint.x, apoint.y),
            mutableListOf(bpoint.x, bpoint.y),
            mutableListOf(start.x, start.y),
            mutableListOf(end.x, end.y),
        )

        graphList[graphIndex].paths?.map {
            result.add(mutableListOf(it.x, it.y))
        }

        return result
    }

    fun setEditModel(on: Boolean) {
        isEditModel = on
        invalidate()
    }

    fun clearGraph() {
        graphList[graphIndex] = Graph()
        invalidate()
    }


}