package com.example.amaptest.rect

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.amaptest.R
import kotlin.math.min


/**
 * 用于显示区域和座位
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
    private val textFillPaint = TextPaint().apply {
        textAlign = Paint.Align.CENTER
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

    // Data
    // 区域和座位列表数据
    private var seatArea: SeatArea? = null
    private var seats: List<SeatData> = emptyList()

    // 坐标系映射比例
    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f

    // 用于处理点击事件
    private var touchedSeat: SeatData? = null

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.SeatView, defStyleAttr, 0) {
                getColor(R.styleable.SeatView_sv_labelTextColor, Color.BLACK).let {
                    textFillPaint.color = it
                }
                getDimension(R.styleable.SeatView_sv_labelTextSize, 24f).let {
                    textFillPaint.textSize = it
                }
            }
        }
    }

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
        val seatArea = seatArea
        if (seatArea == null) {
            super.onMeasure(0, 0)
            return
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        println("_____ onMeasure widthM ${getModeString(widthMode)} widthSize $widthSize heightM ${getModeString(heightMode)} heightSize $heightSize")

        if (widthMode == MeasureSpec.UNSPECIFIED ) throw UnsupportedOperationException("widthMode")

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            val scale = calculateScales(seatArea, widthSize, heightSize)
            scaleX = scale.first
            scaleY = scale.first

            setMeasuredDimension(widthSize, (seatArea.areaHeight * scaleY).toInt())
        } else {
            val scale = calculateScales(seatArea,widthSize, heightSize)
            scaleX = scale.first
            scaleY = scale.first
            val areaH = (seatArea.areaHeight * scaleY).toInt()
            val h = min(heightSize, areaH)

            setMeasuredDimension(widthSize, h)
        }
    }

    /**
     * 计算映射比例。
     */
    private fun calculateScales(seatArea: SeatArea,width: Int, height: Int): Pair<Float, Float> {
        if (seatArea.areaWidth <= 0 || seatArea.areaHeight <= 0) {
            throw IllegalArgumentException("data logic error")
        }

        val w = width.toFloat()
        val h = height.toFloat()
        return Pair(w / seatArea.areaWidth, h / seatArea.areaHeight)
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

        // 遍历并绘制每一个座位
        for (seat in seats) {
            // 获取对应状态的UI数据
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

            // 应用映射比例，计算在画布上的实际位置和大小
            val left = seat.x * scaleX
            val top = seat.y * scaleY
            val right = (seat.x + seat.width) * scaleX
            val bottom = (seat.y + seat.height) * scaleY

            // 绘制座位背景
            seatFillPaint.color = backgroundColor
            canvas.drawRect(left, top, right, bottom, seatFillPaint)

            // 绘制座位边框
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

            // 文字
            if (!seat.title.isNullOrBlank()) {
                val centerX = left + (right - left) / 2
                val centerY = top + (bottom - top) / 2

                val fontMetrics = textFillPaint.fontMetrics
                val baselineY = centerY - (fontMetrics.descent + fontMetrics.ascent) / 2
                //  canvas.drawPoint(centerX, centerY, borderPaint) // auxiliary
                canvas.drawText(seat.title.orEmpty(), centerX, baselineY, textFillPaint)
            }
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
                // 检查抬起时是否仍在同一个座位上
                if (touchedSeat != null && seatAtUp == touchedSeat) {
                    // 确认点击事件，切换座位状态
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

    fun getModeString(mode: Int): String {
        return when (mode) {
            MeasureSpec.EXACTLY -> "EXACTLY"
            MeasureSpec.AT_MOST -> "AT_MOST"
            MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
            else -> "Unknown mode: $mode"
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
    var title: String? = null,
)

enum class SeatStatus {
    Disable,
    Checked,
    UnChecked,
}

