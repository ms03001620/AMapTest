package com.example.amaptest.rect


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    // Configuration
    var mainLineColor = Color.RED
    var auxiliaryLineColor = Color.YELLOW
    var textColor = Color.GREEN
    var padding = 20
    var lineWidth = 4f
    var screenSize = Point(704, 576) // Default screen size
    var arrowLength = 28

    // State
    private var startPoint: Point? = null
    private var endPoint: Point? = null
    private var aPoint: Point? = null
    private var bPoint: Point? = null
    private var isSwapped = false

    // Paint objects
    private val mainLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = mainLineColor
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


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val start = startPoint
        val end = endPoint

        // Draw main line
        if (start != null && end != null) {
            canvas.drawLine(
                start.x.toFloat(),
                start.y.toFloat(),
                end.x.toFloat(),
                end.y.toFloat(),
                mainLinePaint
            )
            // Draw "Result" label at the start point
            drawLabel(canvas, start, "Result")
        }

        val ap = aPoint
        val bp = bPoint

        // Draw auxiliary line and labels
        if (ap != null && bp != null) {
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
            drawLabel(canvas, ap, if (!isSwapped) "A" else "B")
            drawLabel(canvas, bp, if (!isSwapped) "B" else "A")
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint = Point(event.x.roundToInt(), event.y.roundToInt())
                aPoint = null
                bPoint = null
                invalidate() // Redraw
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                endPoint = Point(event.x.roundToInt(), event.y.roundToInt())
                aPoint = null
                bPoint = null
                invalidate() // Redraw
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                endPoint = Point(
                    max(0, min(width, event.x.roundToInt())),
                    max(0, min(height, event.y.roundToInt())),
                )

                val start = startPoint
                val end = endPoint
                if (start != null && end != null) {
                    generateAuxiliaryLine(
                        start,
                        end
                    ).let {
                        aPoint = it.first
                        bPoint = it.second
                    }
                }

                invalidate() // Redraw
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun generateAuxiliaryLine(
        startPoint: Point,
        endPoint: Point,
    ): Pair<Point, Point> {
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
    ) : Pair<Point, Point> {
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


    fun calculateDistance(p1: Point, p2: Point): Double {
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
}