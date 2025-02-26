package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

data class DrawRectInfo(
    val screenSize: Point = Point(704, 576),
    val max: Int = 3,
    val data: List<Shelter>,
    val textSize: Int = 30,
    val color: Int = Color.RED,
    val colorFull: Int = Color.BLACK,
    val borderWidth: Int = 4,
)

data class Shelter(
    var hideAreaTopLeftX: Int? = null,
    var hideAreaTopLeftY: Int? = null,
    var hideAreaWidth: Int? = null,
    var hideAreaHeight: Int? = null,
)

@SuppressLint("ClickableViewAccessibility")
class PrivacyRectView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var drawRectInfo: DrawRectInfo? = null
    private var currentRect: Rect? = null
    private var isDrawing = false
    private var isEditing = false
    private val rectList = mutableListOf<Shelter>()

    // Drawing tools
    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val fullPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f

    fun setDrawRectInfo(drawRectInfo: DrawRectInfo) {
        this.drawRectInfo = drawRectInfo
        initView()
        postInvalidate()
    }

    private fun initView() {
        drawRectInfo?.let { info ->
            borderPaint.color = info.color
            borderPaint.strokeWidth = info.borderWidth.toFloat()
            fullPaint.color = info.colorFull
            textPaint.textSize = info.textSize.toFloat()
            textPaint.color = info.color
            rectList.addAll(info.data)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectList.forEachIndexed { index, shelter ->
            val rect = mapShelterToScreenRect(shelter)
            if (rect != null) {
                if (isEditing) {
                    canvas.drawRect(rect, borderPaint)
                } else {
                    canvas.drawRect(rect, fullPaint)
                    canvas.drawRect(rect, borderPaint)
                }
                val text = (index + 1).toString()
                val textX = rect.centerX().toFloat()
                val textY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(text, textX, textY, textPaint)
            }
        }
        currentRect?.let {
            canvas.drawRect(it, borderPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEditing) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDrawing = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    endX = event.x
                    endY = event.y
                    updateCurrentRect()
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (isDrawing) {
                    endX = event.x
                    endY = event.y
                    updateCurrentRect()
                    addRect()
                    invalidate()
                    isDrawing = false
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                if (isDrawing) {
                    isDrawing = false
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updateCurrentRect() {
        val left = min(startX, endX)
        val top = min(startY, endY)
        val right = max(startX, endX)
        val bottom = max(startY, endY)

        val clampedLeft = max(0f, left)
        val clampedTop = max(0f, top)
        val clampedRight = min(width.toFloat(), right)
        val clampedBottom = min(height.toFloat(), bottom)

        currentRect = Rect(clampedLeft.toInt(), clampedTop.toInt(), clampedRight.toInt(), clampedBottom.toInt())
    }

    private fun addRect() {
        currentRect?.let { rect ->
            if (rectList.size < drawRectInfo!!.max) {
                val shelter = mapScreenRectToShelter(rect)
                rectList.add(shelter)
            }
            currentRect = null
        }
    }

    private fun mapShelterToScreenRect(shelter: Shelter): Rect? {
        drawRectInfo?.let { info ->
            val screenWidth = width.toFloat()
            val screenHeight = height.toFloat()
            val targetWidth = info.screenSize.x.toFloat()
            val targetHeight = info.screenSize.y.toFloat()

            val left = (shelter.hideAreaTopLeftX?.toFloat() ?: 0f) / targetWidth * screenWidth
            val top = (shelter.hideAreaTopLeftY?.toFloat() ?: 0f) / targetHeight * screenHeight
            val right = ((shelter.hideAreaTopLeftX ?: 0) + (shelter.hideAreaWidth ?: 0)).toFloat() / targetWidth * screenWidth
            val bottom = ((shelter.hideAreaTopLeftY ?: 0) + (shelter.hideAreaHeight ?: 0)).toFloat() / targetHeight * screenHeight

            return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }
        return null
    }

    private fun mapScreenRectToShelter(rect: Rect): Shelter {
        drawRectInfo?.let { info ->
            val screenWidth = width.toFloat()
            val screenHeight = height.toFloat()
            val targetWidth = info.screenSize.x.toFloat()
            val targetHeight = info.screenSize.y.toFloat()

            val x = (rect.left / screenWidth * targetWidth).toInt()
            val y = (rect.top / screenHeight * targetHeight).toInt()
            val w = (rect.width() / screenWidth * targetWidth).toInt()
            val h = (rect.height() / screenHeight * targetHeight).toInt()

            return Shelter(x, y, w, h)
        }
        return Shelter()
    }

    fun enableEdit() {
        isEditing = true
        rectList.clear()
        invalidate()
    }

    fun finishEdit() {
        isEditing = false
        invalidate()
    }

    fun getResult(): List<Shelter> {
        return if (rectList.isNotEmpty()) rectList.toList() else emptyList()
    }
}