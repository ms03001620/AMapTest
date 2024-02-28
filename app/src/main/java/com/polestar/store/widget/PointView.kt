package com.polestar.store.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.amaptest.R
import com.example.amaptest.databinding.CmPointBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.min

class PointView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    val binding = CmPointBinding.inflate(LayoutInflater.from(context), this, true)
    private val translateAnimation =
        AnimationUtils.loadAnimation(context, R.anim.charging_anim_point_float)

    fun setPoint(point: Int) {
        if (point > 0) {
            showIdlePoint(point)
        } else {
            showAlreadyGetPoint()
        }
    }

    fun updateSeconds(countDownNumber: Int, total: Int) {
        val number = total - countDownNumber
        val p = min(100, ((number / total.toFloat()) * 100).toInt())
        binding.progress.progress = p
    }

    fun doneWithAnim(total: Int, point: Int, lifecycleScope: LifecycleCoroutineScope) {
        lifecycleScope.launch {
            delay(500)
            showOldTotalPoint(total, point)

            delay(600)
            showAddPoint(point) {
                showTotalPoint(total, point, lifecycleScope) {
                    lifecycleScope.launch {
                        delay(1000)
                        showAlreadyGetPoint()
                    }
                }
            }
        }
    }

    private fun pointIncrementally(
        textView: TextView,
        total: Int,
        point: Int,
        interval: Long,
        animTotalFrame: Int,
        scope: LifecycleCoroutineScope,
        onAnimationEndCallback: () -> Unit
    ) {
        var pointBase = total - point

        scope.launch {
            PointSplit.splitPoint(point, animTotalFrame).map {
                pointBase += it
                pointBase
            }.forEach {
                println(it)
                textView.setText(it.toString())
                delay(interval)
            }.let {
                onAnimationEndCallback.invoke()
            }
        }
    }

    private fun showTotalPoint(
        total: Int,
        point: Int,
        scope: LifecycleCoroutineScope,
        onAnimationEndCallback: () -> Unit
    ) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = false
        binding.done.isVisible = false
        binding.add.isVisible = false
        pointIncrementally(
            textView = binding.point,
            total = total,//总分，动画结束后停留的分数
            point = point,//本次增加的积分
            scope = scope,
            interval = 200L,//每次加分间隔
            animTotalFrame = 5,//动画帧数，加分次数
            onAnimationEndCallback = onAnimationEndCallback,
        )
    }

    private fun showAddPoint(point: Int, onAnimationEndCallback: () -> Unit) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = false
        binding.point.isVisible = true
        binding.done.isVisible = false
        binding.add.text = "+$point"
        binding.add.isVisible = true

        // 显示+50分并移动向上移动到消失
        AnimationUtils.loadAnimation(context, R.anim.charging_anim_point_float).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    onAnimationEndCallback.invoke()
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            binding.add.startAnimation(this)
        }
    }

    private fun showOldTotalPoint(total: Int, point: Int) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = false
        binding.point.text = "${total - point}"
        binding.done.isVisible = false
        binding.add.isVisible = false
    }

    private fun showIdlePoint(point: Int) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = true
        binding.point.text = "$point"
        binding.done.isVisible = false
        binding.add.isVisible = false
    }

    private fun showAlreadyGetPoint() {
        binding.layoutPoint.isVisible = false
        binding.done.isVisible = true
        binding.progress.progress = 0
        binding.add.isVisible = false
    }
}