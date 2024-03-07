package com.polestar.charging.ui.carprocess

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.animation.LinearInterpolator
import androidx.appcompat.content.res.AppCompatResources
import com.polestar.base.ext.dp
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class StickLayout(
    context: Context,
    private val paddingStartOfCar: Int,
    private val paddingEndOfCar: Int,
    private val stickResId: Int,
    private var onRequestInvalidate: (() -> Unit)?,
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

    fun startCharging(isCharging: Boolean) {
        this.isCharging = isCharging

        if (isCharging) {
            stopAnimation()
            startAnimation()
        } else {
            stopAnimation()
            onRequestInvalidate?.invoke()
        }
    }

    fun process(process: Float) {
        // process 减少时，主要立即更新动画
        // process 增加时，不需要停止动画，动画会在下一轮更新最新的process
        if (process < this.process && isCharging) {
            stopAnimation()
            startAnimation()
        }

        this.process = process
    }

    private fun stopAnimation() {
        animator?.removeAllListeners()
        animator?.cancel()
        animator = null
    }

    fun release() {
        onRequestInvalidate = null
        stopAnimation()
    }

    private fun startAnimation() {
        if (process > 0 && isCharging) {
            val xOffset = (carWidth * process).roundToInt()

            animator = ValueAnimator.ofInt(paddingStartOfCar, paddingStartOfCar + xOffset).apply {
                repeatCount = 1
                duration = durationAnim - (500 * process).roundToLong()//进度越大，速度越快
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val start = value - stickWidth

                    stickDrawable.setBounds(start, 0, value, height)
                    onRequestInvalidate?.invoke()
                }
                addListener(object: Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        // 手动实现动画循环，为了新的动画可以后去最新的process
                        stopAnimation()
                        startAnimation()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }
                })
            }
            animator?.start()
        }
    }
}