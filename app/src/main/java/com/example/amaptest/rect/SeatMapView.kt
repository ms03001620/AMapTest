package jp.linktivity.citypass.temp.four;

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import jp.linktivity.citypass.temp.one.Seat
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.withMatrix

class SeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val seats = mutableListOf<Seat>()

    private val seatPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.LTGRAY }
    private val selectedPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.GREEN }

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
    private val miniViewPortPaint =
        Paint().apply { style = Paint.Style.STROKE; color = Color.RED; strokeWidth = 3f }
    private val miniMapSize = 300f
    private val miniMapMargin = 50f

    // 座位选中回调
    var onSeatSelected: ((Seat) -> Unit)? = null

    fun setSeats(list: List<Seat>) {
        seats.clear()
        seats.addAll(list)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (seats.isEmpty()) return

        canvas.withMatrix(matrix) {
            // 绘制座位
            for (seat in seats) {
                val rect = RectF(seat.x, seat.y, seat.x + seat.width, seat.y + seat.height)
                drawRect(rect, if (seat.selected) selectedPaint else seatPaint)
            }
        }

        // 绘制缩略图
        drawMiniMap(canvas)
    }

    private fun drawMiniMap(canvas: Canvas) {
        val bounds = getSeatsBounds() ?: return
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
        canvas.drawRect(miniRect, miniViewPortPaint)
    }

    private fun getSeatsBounds(): RectF? {
        if (seats.isEmpty()) return null

        // 当 x, y 是左上角坐标时：
        // 左边界就是所有 seat.x 中的最小值
        val left = seats.minOf { it.x }
        // 上边界就是所有 seat.y 中的最小值
        val top = seats.minOf { it.y }

        // 右边界是所有 (seat.x + seat.width) 中的最大值
        val right = seats.maxOf { it.x + it.width }
        // 下边界是所有 (seat.y + seat.height) 中的最大值
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
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
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
            val seat = findSeatByPoint(e.x, e.y)
            if (seat != null) {
                seat.selected = !seat.selected
                onSeatSelected?.invoke(seat)
                invalidate()
                return true
            }
            return false
        }
    }

    fun findSeatByPoint(touchX: Float, touchY: Float): Seat? {
        val points = floatArrayOf(touchX, touchY)
        val tempInverseMatrix = Matrix()
        matrix.invert(tempInverseMatrix)
        tempInverseMatrix.mapPoints(points)

        val worldX = points[0]
        val worldY = points[1]

        for (seat in seats) {
            val seatRect = RectF(seat.x, seat.y, seat.x + seat.width, seat.y + seat.height)
            if (seatRect.contains(worldX, worldY)) {
                return seat
            }
        }
        return null
    }

}
