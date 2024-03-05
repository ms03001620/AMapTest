package com.polestar.charging.ui.carprocess

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import polestar.base.ext.dp
import kotlin.math.roundToInt

class StickLayout(
    context: Context,
    private val paddingStartOfCar: Int,
    private val paddingEndOfCar: Int,
    private val stickResId: Int,
    private val onRequestInvalidate: () -> Unit,
) {
    private val durationAnim = 2000L
    private var stickWidth = 60.dp
    private val stickDrawable: Drawable
    private var animator: ValueAnimator? = null
    private var carWidth = 0
    private var height = 0
    private var isCharging = false
    private var process: Float = 0.0f

    init {
        stickDrawable = AppCompatResources.getDrawable(context, stickResId)
            ?: throw NotFoundException("charging_stick")
    }

    fun onMeasure(width: Int, height: Int) {
        this.height = height
        stickDrawable.setBounds(0, 0, stickWidth, height)
        carWidth = width - paddingStartOfCar - paddingEndOfCar
    }

    fun onDraw(canvas: Canvas, process: Float) {
        this.process = process
        if (process > 0 && isCharging) {
            stickDrawable.draw(canvas)
        }
    }

    fun setCharging(isCharging: Boolean) {
        this.isCharging = isCharging

        if (isCharging) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    fun process(process: Float) {
        this.process = process
        startAnimation()
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    fun startAnimation() {
        stopAnimation()
        if (process > 0 && isCharging) {
            val xOffset = (carWidth * process).roundToInt()

            animator = ValueAnimator.ofInt(paddingStartOfCar, paddingStartOfCar + xOffset).apply {
                repeatCount = ValueAnimator.INFINITE
                duration = durationAnim
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val start = value - stickWidth

                    stickDrawable.setBounds(start, 0, value, height)
                    onRequestInvalidate.invoke()
                }
            }
            animator?.start()
        }
    }
}