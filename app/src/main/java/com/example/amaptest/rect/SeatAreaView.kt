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

        calculateScales()
        invalidate()
    }

    /**
     * 计算映射比例。
     */
    private fun calculateScales() {
        seatArea?.let {
            if (it.areaWidth > 0 && it.areaHeight > 0) {
                scaleX = width.toFloat() / it.areaWidth
                scaleY = height.toFloat() / it.areaHeight
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 布局变化后计算映射比例
        calculateScales()
    }

    /**
     * 绘制View内容。
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制背景
        if (seatArea == null) {
            // 如果没有数据，则不绘制任何内容
            return
        }
        canvas.drawColor(backgroundColor)

        // 遍历并绘制每一个座位
        for (seat in seats) {
            // 获取对应状态的UI数据
            val ui = seatStatusUiMap[seat.seatStatus] ?: continue // 如果找不到则跳过

            // 应用映射比例，计算在画布上的实际位置和大小
            val left = seat.x * scaleX
            val top = seat.y * scaleY
            val right = (seat.x + seat.width) * scaleX
            val bottom = (seat.y + seat.height) * scaleY


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

            // 绘制座位背景
            seatFillPaint.color = backgroundColor
            canvas.drawRect(left, top, right, bottom, seatFillPaint)

            // 绘制座位边框
            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth.toFloat()
            canvas.drawRect(left, top, right, bottom, borderPaint)
        }
    }

    /**
     * 处理触摸事件。
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 将触摸坐标从画布坐标系转换回数据坐标系
        val touchX = event.x / scaleX
        val touchY = event.y / scaleY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 查找被按下的座位
                touchedSeat = findSeatAt(touchX, touchY)
                // 如果找到了一个有效的座位，则消费此事件
                return touchedSeat != null
            }

            MotionEvent.ACTION_UP -> {
                val seatAtUp = findSeatAt(touchX, touchY)
                // 检查抬起时是否仍在同一个座位上
                if (touchedSeat != null && seatAtUp == touchedSeat) {
                    // 确认点击事件，切换座位状态
                    toggleSeatStatus(touchedSeat!!)
                }
                // 重置被触摸的座位
                touchedSeat = null
            }

            MotionEvent.ACTION_CANCEL -> {
                // 事件被取消，重置
                touchedSeat = null
            }
        }
        return true // 消费事件
    }

    /**
     * 查找给定数据坐标下的座位。
     */
    private fun findSeatAt(dataX: Float, dataY: Float): SeatData? {
        // 从后往前遍历，这样顶层的座位会被优先选中
        return seats.lastOrNull { seat ->
            // 进行边界检测
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
            // Disable状态不响应点击，所以这里不需要处理
            SeatStatus.Disable -> seat.seatStatus
        }
        // 请求重新绘制以更新UI
        invalidate()
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

