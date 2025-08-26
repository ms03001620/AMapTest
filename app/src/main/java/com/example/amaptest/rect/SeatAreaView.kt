package com.example.amaptest.rect

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * 一个自定义View，用于显示和管理座位布局。
 *
 * A custom view for displaying and managing a seat layout.
 */
class SeatAreaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // UI
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val seatFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // 座位状态UI
    private val seatStatusUiMap: Map<SeatStatus, SeatStatusUi> = mapOf(
        SeatStatus.Disable to SeatStatusUi.Disable(
            borderWidth = 2,
            borderColor = Color.BLACK,
            backgroundColor = Color.LTGRAY
        ),
        SeatStatus.Checked to SeatStatusUi.Checked(
            borderWidth = 2,
            borderColor = Color.BLACK,
            backgroundColor = Color.RED
        ),
        SeatStatus.UnChecked to SeatStatusUi.UnChecked(
            borderWidth = 2,
            borderColor = Color.BLACK,
            backgroundColor = Color.WHITE
        )
    )
    // 整体背景色
    private val backgroundColor = 0x2222

    // Data
    // 区域和座位列表数据
    private var seatArea: SeatArea? = null
    private var seats: List<SeatData> = emptyList()

    // 坐标系映射比例
    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f

    // 用于处理点击事件
    private var touchedSeat: SeatData? = null

    /**
     * 设置座位区域和座位列表数据。
     */
    fun setData(seatArea: SeatArea, seats: List<SeatData>) {
        this.seatArea = seatArea
        this.seats = seats

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = getModeString(MeasureSpec.getMode(widthMeasureSpec))
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = getModeString(MeasureSpec.getMode(heightMeasureSpec))
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode == "UNSPECIFIED") throw UnsupportedOperationException("widthMode")

        val scale = calculateScales(widthSize, heightSize)
        if (scale != null) {
            scaleX = scale.first
            scaleY = scale.first
        }

        if (heightMode == "UNSPECIFIED" && seatArea != null) {
            setMeasuredDimension(widthSize, (seatArea!!.areaHeight * scaleY).toInt())
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * 计算映射比例。
     */
    private fun calculateScales(width: Int, height: Int): Pair<Float, Float>? {
        seatArea?.let {
            if (it.areaWidth > 0 && it.areaHeight > 0) {
                val w = width.toFloat()
                val h = height.toFloat()
                return Pair(w / it.areaWidth, h / it.areaHeight)
            }
        }
        return null
    }

    /**
     * 绘制View内容。
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (seatArea == null) {
            return
        }

        // 绘制背景
        canvas.drawColor(backgroundColor)

        // 绘制每一个座位
        for (seat in seats) {
            val ui = seatStatusUiMap[seat.seatStatus] ?: continue // 如果找不到则跳过

            val borderWidth = when (ui) {
                is SeatStatusUi.Disable -> ui.borderWidth
                is SeatStatusUi.Checked -> ui.borderWidth
                is SeatStatusUi.UnChecked -> ui.borderWidth
            }

            val backgroundColor = when (ui) {
                is SeatStatusUi.Disable -> ui.backgroundColor
                is SeatStatusUi.Checked -> ui.backgroundColor
                is SeatStatusUi.UnChecked -> ui.backgroundColor
            }

            val borderColor = when (ui) {
                is SeatStatusUi.Disable -> ui.borderColor
                is SeatStatusUi.Checked -> ui.borderColor
                is SeatStatusUi.UnChecked -> ui.borderColor
            }

            val left = seat.x * scaleX
            val top = seat.y * scaleY
            val right = (seat.x + seat.width) * scaleX
            val bottom = (seat.y + seat.height) * scaleY

            seatFillPaint.color = backgroundColor
            canvas.drawRect(left, top, right, bottom, seatFillPaint)

            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth.toFloat()

            val halfStroke: Float = borderWidth / 2f
            canvas.drawRect(
                left + halfStroke,
                top + halfStroke,
                right - halfStroke,
                bottom - halfStroke,
                borderPaint
            )
        }
    }

    /**
     * 处理触摸事件。
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x / scaleX
        val touchY = event.y / scaleY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchedSeat = findSeatAt(touchX, touchY)
                return touchedSeat != null
            }

            MotionEvent.ACTION_UP -> {
                val seatAtUp = findSeatAt(touchX, touchY)
                if (touchedSeat != null && seatAtUp == touchedSeat) {
                    toggleSeatStatus(touchedSeat!!)
                }
                touchedSeat = null
            }

            MotionEvent.ACTION_CANCEL -> {
                touchedSeat = null
            }
        }
        return true
    }

    /**
     * 查找给定数据坐标下的座位。
     */
    private fun findSeatAt(dataX: Float, dataY: Float): SeatData? {
        return seats.lastOrNull { seat ->
            val isWithinX = dataX >= seat.x && dataX < (seat.x + seat.width)
            val isWithinY = dataY >= seat.y && dataY < (seat.y + seat.height)
            isWithinX && isWithinY && seat.seatStatus != SeatStatus.Disable
        }
    }

    /**
     * 切换座位状态 (Checked <-> UnChecked)。
     */
    private fun toggleSeatStatus(seat: SeatData) {
        seat.seatStatus = when (seat.seatStatus) {
            SeatStatus.Checked -> SeatStatus.UnChecked
            SeatStatus.UnChecked -> SeatStatus.Checked
            SeatStatus.Disable -> seat.seatStatus
        }
        invalidate()
    }

    fun getModeString(measureSpec: Int): String {
        val mode = MeasureSpec.getMode(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> "EXACTLY"
            MeasureSpec.AT_MOST -> "AT_MOST"
            MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
            else -> throw UnsupportedOperationException("Unknown mode: $mode")
        }
    }
}

sealed class SeatStatusUi {

    class Disable(
        val borderWidth: Int,
        val borderColor: Int,
        val backgroundColor: Int,
    ) : SeatStatusUi()

    class Checked(
        val borderWidth: Int,
        val borderColor: Int,
        val backgroundColor: Int,
    ) : SeatStatusUi()

    class UnChecked(
        val borderWidth: Int,
        val borderColor: Int,
        val backgroundColor: Int,
    ) : SeatStatusUi()
}

data class SeatArea(
    val areaWidth: Int,
    val areaHeight: Int,
)

data class SeatData(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    var seatStatus: SeatStatus,
)

enum class SeatStatus {
    Disable,
    Checked,
    UnChecked,
}

