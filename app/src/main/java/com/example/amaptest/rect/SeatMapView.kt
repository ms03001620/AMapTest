package jp.linktivity.citypass.temp.four;

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import jp.linktivity.citypass.temp.one.Seat
import kotlin.math.max
import androidx.core.graphics.withMatrix
import kotlin.math.pow
import kotlin.math.sqrt

class SeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val seats = mutableListOf<Seat>()

    private var seatsRectList: List<SeatRect>? = null

    private var mapRect: RectF? = null
    private lateinit var mapCenter: Point

    private val seatPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.LTGRAY }
    private val selectedPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.GREEN }
    private val testPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.GREEN; strokeWidth = 8f }

    // Matrix 控制缩放和平移
    private val matrix = Matrix()
    private val inverseMatrix = Matrix()

    // 缩放
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    // 点击
    private val gestureDetector = GestureDetector(context, GestureListener())

    // 滑动地图
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastTouch = false

    // MiniMap
    private val miniMapPaint = Paint().apply { color = Color.argb(100, 255, 0, 0) }
    private val miniMapWindow =
        Paint().apply { style = Paint.Style.STROKE; color = Color.RED; strokeWidth = 2f }
    private val miniMapSize = 180f
    private val miniMapMargin = 50f

    // 座位选中回调
    var onSeatSelected: ((Seat) -> Unit)? = null

    fun setSeats(list: List<Seat>) {
        if (list.isEmpty()) return

        seats.clear()
        seats.addAll(list)

        seatsRectList = list.map { seat ->
            SeatRect(
                id = seat.id,
                selected = seat.selected,
                rect = RectF(seat.x, seat.y, seat.x + seat.width, seat.y + seat.height)
            )
        }

        mapRect = getSeatsBounds()
        mapCenter = Point(mapRect!!.centerX().toInt(), mapRect!!.centerY().toInt())
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = seatsRectList ?: return
        canvas.withMatrix(matrix) {
            // 绘制座位
            for (seat in data) {
               drawRect(seat.rect, if (seat.selected) selectedPaint else seatPaint)
            }
            canvas.drawPoint(mapCenter.x.toFloat(), mapCenter.y.toFloat(), testPaint)
        }

        canvas.drawPoint(windowBounds.centerX(), windowBounds.centerY(), testPaint)

        // 绘制缩略图
        drawMiniMap(canvas)
    }

    lateinit var windowBounds: RectF

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        windowBounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
    }

    private fun drawMiniMap(canvas: Canvas) {
        val bounds = mapRect ?: return
        val scale = miniMapSize / max(bounds.width(), bounds.height())
        val left = width - miniMapSize - miniMapMargin
        val top = miniMapMargin
        val right = left + bounds.width() * scale
        val bottom = top + bounds.height() * scale

        canvas.drawRect(left, top, right, bottom, miniMapPaint)

        for (seat in seats) {
            val miniX = left + (seat.x - bounds.left) * scale
            val miniY = top + (seat.y - bounds.top) * scale
            val miniW = seat.width * scale
            val miniH = seat.height * scale
            val rect =
                RectF(miniX , miniY , miniX+miniW, miniY+miniH )
            canvas.drawRect(rect, if (seat.selected) selectedPaint else seatPaint)
        }

        // 绘制视口
        val points = floatArrayOf(0f, 0f, width.toFloat(), height.toFloat())
        matrix.invert(inverseMatrix)
        inverseMatrix.mapPoints(points)
        val miniRect = RectF(
            left + (points[0] - bounds.left) * scale,
            top + (points[1] - bounds.top) * scale,
            left + (points[2] - bounds.left) * scale,
            top + (points[3] - bounds.top) * scale
        )
        canvas.drawRect(miniRect, miniMapWindow)
    }

    private fun getSeatsBounds(): RectF? {
        val left = seats.minOf { it.x }
        val top = seats.minOf { it.y }
        val right = seats.maxOf { it.x + it.width }
        val bottom = seats.maxOf { it.y + it.height }
        return RectF(left, top, right, bottom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleDetector.onTouchEvent(event)

        if(scaleDetector.isInProgress){
            lastTouch = false
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch = true
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if(lastTouch){
                    var dx = event.x - lastTouchX
                    var dy = event.y - lastTouchY

                    matrix.postTranslate(dx, dy)

                    lastTouchX = event.x
                    lastTouchY = event.y

                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                lastTouch = false
            }
        }
        return true
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val focusX = detector.focusX
            val focusY = detector.focusY
            matrix.postScale(scale, scale, focusX, focusY)
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val seatRect = findSeatByPoint(e.x, e.y)
            if (seatRect != null) {
                seatRect.selected = !seatRect.selected
                val origin = seats.first { it.id == seatRect.id }
                origin.selected = seatRect.selected
                onSeatSelected?.invoke(origin)
                invalidate()
                return true
            }
            return false
        }
    }

    fun findSeatByPoint(touchX: Float, touchY: Float): SeatRect? {
        val data = seatsRectList ?: return null
        val points = floatArrayOf(touchX, touchY)
        val tempInverseMatrix = Matrix()
        matrix.invert(tempInverseMatrix)
        tempInverseMatrix.mapPoints(points)

        val worldX = points[0]
        val worldY = points[1]

        for (seat in data) {
            if (seat.rect.contains(worldX, worldY)) {
                return seat
            }
        }
        return null
    }

    fun calculateDistanceBetweenPointsF(point1: PointF, point2: PointF): Float {
        val dx = point2.x - point1.x
        val dy = point2.y - point1.y
        return sqrt(dx.pow(2) + dy.pow(2))
    }


    data class SeatRect(
        val id: String,
        val rect: RectF,
        var selected: Boolean,
        val disable: Boolean? = false,
    )

}
