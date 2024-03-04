package com.example.amaptest.carprocess

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import com.example.amaptest.R
import com.example.amaptest.ui.main.dp
import kotlin.math.roundToInt

class StickLayout(
    context: Context,
    private val paddingStartOfCar: Int,
    private val paddingEndOfCar: Int
) {
    private val durationAnim = 2000L
    private var stickWidth = 60.dp
    private val stickDrawable: Drawable
    private var animator: ValueAnimator? = null
    private var carWidth = 0
    private var height = 0

    init {
        stickDrawable =
            AppCompatResources.getDrawable(context, R.drawable.bg_stick) ?: throw NotFoundException(
                "bg_stick"
            )
    }

    fun onMeasure(width: Int, height: Int) {
        this.height = height
        stickDrawable.setBounds(0, 0, stickWidth, height)
        carWidth = width - paddingStartOfCar - paddingEndOfCar
    }

    fun onDraw(canvas: Canvas, process: Float) {
        if (process > 0) {
            stickDrawable.draw(canvas)
        }
    }

    fun startAnimation(process: Float, function: () -> Unit) {
        animator?.cancel()
        if (process > 0) {
            val xOffset = (carWidth * process).roundToInt()

            animator = ValueAnimator.ofInt(paddingStartOfCar, paddingStartOfCar + xOffset).apply {
                repeatCount = ValueAnimator.INFINITE
                duration = durationAnim
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val start = value - stickWidth

                    stickDrawable.setBounds(start, 0, value, height)
                    function.invoke()
                }
            }
            animator?.start()
        }
    }
}