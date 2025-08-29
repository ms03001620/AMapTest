package com.example.amaptest.rect

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withMatrix
import com.polestar.base.ext.dp
import jp.linktivity.citypass.temp.one.Seat
import kotlin.math.max
import kotlin.math.min

class SeatMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val seats = mutableListOf<Seat>()
    private var seatsRectList: List<SeatRect>? = null
    private var mapRect: RectF? = null

    //  paints...
    private val seatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.LTGRAY }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = Color.GREEN }

    private val matrix = Matrix()

    //允许越过边界的距离
    private val overScrollOffset = 0f//100f.dp


    // 缩放
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val minScale = 0.8f
    private val maxScale = 5.0f


    // 滑动相关
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private val gestureDetector = GestureDetector(context, GestureListener())

    // MiniMap
    private val inverseMatrix = Matrix()
    private val miniMapPaint = Paint().apply { color = Color.argb(22, 0, 0, 0) }
    private val miniMapWindow =
        Paint().apply { style = Paint.Style.STROKE; color = Color.RED; strokeWidth = 2f }
    private val miniMapSize = 80f.dp
    private val miniMapMargin = 50f
    private var showMiniMap = false
    // 地图显示控制
    private val miniMapHideHandler = Handler(Looper.getMainLooper(), object: Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            showMiniMap = false
            invalidate()
            return true
        }
    })

    // Callback
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

/*        // 当View已经有尺寸时，重置矩阵以显示地图
        if (width > 0 && height > 0) {
            resetMatrix()
        }*/
        invalidate()
    }

    // 1. 新增：重置矩阵，让地图初始时完整居中显示
    private fun resetMatrix() {
        val localMapRect = mapRect ?: return
        matrix.reset()
        // 计算缩放比例，让地图完整显示在View中
        val scaleX = width / localMapRect.width()
        val scaleY = height / localMapRect.height()
        val scale = min(scaleX, scaleY)
        matrix.postScale(scale, scale)

        // 计算平移距离，让地图居中
        val scaledWidth = localMapRect.width() * scale
        val scaledHeight = localMapRect.height() * scale
        val dx = (width - scaledWidth) / 2 - localMapRect.left * scale
        val dy = (height - scaledHeight) / 2 - localMapRect.top * scale
        matrix.postTranslate(dx, dy)
        invalidate()
    }

    private fun getSeatsBounds(): RectF? {
        if (seats.isEmpty()) return null
        val left = seats.minOf { it.x }
        val top = seats.minOf { it.y }
        val right = seats.maxOf { it.x + it.width }
        val bottom = seats.maxOf { it.y + it.height }
        return RectF(left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 在View获得尺寸后，立即重置矩阵
        if (mapRect != null) {
            resetMatrix()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = seatsRectList ?: return
        canvas.withMatrix(matrix) {
            for (seat in data) {
                drawRect(seat.rect, if (seat.selected) selectedPaint else seatPaint)
            }
        }
        // 绘制缩略图
        drawMiniMap(canvas)
    }

    // 2. 彻底修正的 onTouchEvent 逻辑
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 优先让缩放和点击手势处理器处理事件
        val scaleHandled = scaleDetector.onTouchEvent(event)
        val gestureHandled = gestureDetector.onTouchEvent(event)

        // 如果正在缩放，则不处理拖动
        if (scaleDetector.isInProgress) {
            isDragging = false
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                showMiniMap()
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    matrix.postTranslate(dx, dy)
                    // 检查边界
                    checkBounds()
                    invalidate()
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        // 只要有一个手势处理器处理了事件，就认为事件被消费了
        return scaleHandled || gestureHandled || isDragging || super.onTouchEvent(event)
    }

    private fun checkBounds() {
        val localMapRect = mapRect ?: return
        val transformedRect = RectF()
        matrix.mapRect(transformedRect, localMapRect)

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        var dx = 0f
        var dy = 0f

        // 水平检查
        if (transformedRect.width() < viewWidth) {
            // 地图比视图窄，限制其在视图内部移动，但允许向外偏移
            if (transformedRect.left < -overScrollOffset) {
                dx = -overScrollOffset - transformedRect.left
            } else if (transformedRect.right > viewWidth + overScrollOffset) {
                dx = (viewWidth + overScrollOffset) - transformedRect.right
            }
        } else {
            // 地图比视图宽，限制视图不出现超过偏移量的空白
            if (transformedRect.left > overScrollOffset) {
                dx = overScrollOffset - transformedRect.left
            } else if (transformedRect.right < viewWidth - overScrollOffset) {
                dx = (viewWidth - overScrollOffset) - transformedRect.right
            }
        }

        // 垂直检查
        if (transformedRect.height() < viewHeight) {
            // 地图比视图矮，限制其在视图内部移动，但允许向外偏移
            if (transformedRect.top < -overScrollOffset) {
                dy = -overScrollOffset - transformedRect.top
            } else if (transformedRect.bottom > viewHeight + overScrollOffset) {
                dy = (viewHeight + overScrollOffset) - transformedRect.bottom
            }
        } else {
            // 地图比视图高，限制视图不出现超过偏移量的空白
            if (transformedRect.top > overScrollOffset) {
                dy = overScrollOffset - transformedRect.top
            } else if (transformedRect.bottom < viewHeight - overScrollOffset) {
                dy = (viewHeight - overScrollOffset) - transformedRect.bottom
            }
        }

        if (dx != 0f || dy != 0f) {
            matrix.postTranslate(dx, dy)
        }
    }
    private fun showMiniMap() {
        miniMapHideHandler.removeMessages(0)
        miniMapHideHandler.sendEmptyMessageDelayed(0, 2000)
        showMiniMap = true
        invalidate()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (mapRect == null) return false

            val scaleFactor = detector.scaleFactor
            val currentScale = getCurrentScale()

            // 计算应用本次手势缩放后的最终缩放值
            val newScale = currentScale * scaleFactor

            // 如果最终缩放值在我们的限制范围内，则直接使用手势的缩放因子
            // 如果超出了范围，则计算一个修正后的缩放因子，使其刚好达到边界值
            val actualScaleFactor = when {
                newScale < minScale -> minScale / currentScale
                newScale > maxScale -> maxScale / currentScale
                else -> scaleFactor
            }

            matrix.postScale(actualScaleFactor, actualScaleFactor, detector.focusX, detector.focusY)
            showMiniMap()
            checkBounds() // 缩放后检查边界
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        // 返回true消费事件，是让拖动生效的关键
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            showMiniMap()
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

    private fun drawMiniMap(canvas: Canvas) {
        if (!showMiniMap) {
            return
        }
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

        val rectTop = top + (points[1] - bounds.top) * scale
        val miniRectTop = if (rectTop < top) top else rectTop
        val rectBottom = top + (points[3] - bounds.top) * scale
        val miniRectBottom = if (rectBottom > bottom) bottom else rectBottom

        val rectLeft = left + (points[0] - bounds.left) * scale
        val miniRectLeft = if (rectLeft < left) left else rectLeft

        val rectRight = left + (points[2] - bounds.left) * scale
        val miniRectRight = if (rectRight > right) right else rectRight

        val miniRect = RectF(miniRectLeft, miniRectTop, miniRectRight, miniRectBottom,)
        canvas.drawRect(miniRect, miniMapWindow)
        canvas.drawRect(miniRect, miniMapWindow)
    }

    // findSeatByPoint 和 SeatRect data class 不变...
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

    private fun getCurrentScale(): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    data class SeatRect(
        val id: String,
        val rect: RectF,
        var selected: Boolean,
        val disable: Boolean? = false,
    )
}