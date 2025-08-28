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

class SeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val seats = mutableListOf<Seat>()

    private val seatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.LTGRAY }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.GREEN }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.BLACK; strokeWidth = 2f }

    // Matrix 控制缩放和平移
    private val matrix = Matrix()
    private val inverseMatrix = Matrix()
    
    // 手势检测
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // MiniMap
    private val miniMapPaint = Paint().apply { color = Color.argb(100,0,0,0) }
    private val miniViewPortPaint = Paint().apply { style = Paint.Style.STROKE; color = Color.RED; strokeWidth = 3f }
    private val miniMapSize = 200f

    // 座位选中回调
    var onSeatSelected: ((Seat) -> Unit)? = null

     fun setSeats(list: List<Seat>) {
        seats.clear()
        seats.addAll(list)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 应用 matrix
        canvas.save()
        canvas.concat(matrix)

        // 绘制座位
        for (seat in seats) {
            val rect = RectF(
                seat.x - seat.width/2,
                seat.y - seat.height/2,
                seat.x + seat.width/2,
                seat.y + seat.height/2
            )
            canvas.drawRect(rect, if(seat.selected) selectedPaint else seatPaint)
            canvas.drawRect(rect, borderPaint)
        }

        canvas.restore()

        // 绘制缩略图
        drawMiniMap(canvas)
    }

    private fun drawMiniMap(canvas: Canvas) {
        if(seats.isEmpty()) return

        val bounds = getSeatsBounds() ?: return
        val scale = miniMapSize / max(bounds.width(), bounds.height())
        val left = width - miniMapSize - 20
        val top = 20f
        val right = left + bounds.width()*scale
        val bottom = top + bounds.height()*scale

        canvas.drawRect(left, top, right, bottom, miniMapPaint)

        for(seat in seats){
            val miniX = left + (seat.x - bounds.left)*scale
            val miniY = top + (seat.y - bounds.top)*scale
            val miniW = seat.width * scale
            val miniH = seat.height * scale
            val rect = RectF(miniX - miniW/2, miniY - miniH/2, miniX + miniW/2, miniY + miniH/2)
            canvas.drawRect(rect, if(seat.selected) selectedPaint else seatPaint)
        }

        // 绘制视口
        val points = floatArrayOf(0f,0f, width.toFloat(), height.toFloat())
        matrix.invert(inverseMatrix)
        inverseMatrix.mapPoints(points)
        val miniRect = RectF(
            left + (points[0]-bounds.left)*scale,
            top + (points[1]-bounds.top)*scale,
            left + (points[2]-bounds.left)*scale,
            top + (points[3]-bounds.top)*scale
        )
        canvas.drawRect(miniRect, miniViewPortPaint)
    }

    private fun getSeatsBounds(): RectF? {
        if(seats.isEmpty()) return null
        val left = seats.minOf { it.x - it.width/2 }
        val top = seats.minOf { it.y - it.height/2 }
        val right = seats.maxOf { it.x + it.width/2 }
        val bottom = seats.maxOf { it.y + it.height/2 }
        return RectF(left, top, right, bottom)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if(!scaleDetector.isInProgress){
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    matrix.postTranslate(dx, dy)
                    invalidate()
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
        }
        return true
    }

    private inner class ScaleListener: ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor
            val focusX = detector.focusX
            val focusY = detector.focusY
            matrix.postScale(scale, scale, focusX, focusY)
            invalidate()
            return true
        }
    }

    private inner class GestureListener: GestureDetector.SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val pts = floatArrayOf(e.x, e.y)
            matrix.invert(inverseMatrix)
            inverseMatrix.mapPoints(pts)
            val x = pts[0]
            val y = pts[1]
            for(seat in seats){
                val rect = RectF(
                    seat.x - seat.width/2,
                    seat.y - seat.height/2,
                    seat.x + seat.width/2,
                    seat.y + seat.height/2
                )
                if(rect.contains(x, y)){
                    seat.selected = !seat.selected
                    onSeatSelected?.invoke(seat)
                    invalidate()
                    break
                }
            }
            return true
        }
    }
}
