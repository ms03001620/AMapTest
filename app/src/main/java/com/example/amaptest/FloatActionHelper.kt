package com.example.amaptest

import android.animation.Animator
import android.content.res.Resources
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.view.isVisible
import com.example.amaptest.databinding.ChargingContentFabsBinding

class FloatActionHelper(
    private val binding: ChargingContentFabsBinding,
    private val resources: Resources
) {
    private var isExpand = false
    private val viewGap: Float
    private val viewSize: Float
    private var isChargingStatus = false

    private val hideButtons = listOf(
        binding.fabHelp,
        binding.fabOrders,
        binding.fabFavorite
    )

    init {
        binding.fabMore.setOnClickListener {
            if (!isExpand) {
                isExpand = true
                expandMenu()
            } else {
                isExpand = false
                closeMenu()
            }
            updateBtnOpenStatus()
        }
        viewGap = toPx(R.dimen.charging_station_fab_gap)
        viewSize = toPx(R.dimen.charging_station_fab_size)
    }

    private fun toPx(resId: Int) = resources.getDimension(resId)

    private fun expandMenu() {
        val anchor = viewSize
        hideButtons.forEachIndexed { index, view ->
            view.visibility = View.VISIBLE
            view.animate()
                .setDuration(ANIM_DURATION)
                .translationY(-(anchor * (index + 1)))
                .alpha(1.0f)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
        }
    }

    private fun closeMenu() {
        hideButtons.forEachIndexed { index, view ->
            view.animate()
                .setDuration(ANIM_DURATION)
                .translationY(0f)
                .alpha(0.0f)
                .setInterpolator(AccelerateInterpolator())
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animator: Animator) {}
                    override fun onAnimationEnd(animator: Animator) {
                        view.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animator: Animator) {}
                    override fun onAnimationRepeat(animator: Animator) {}
                })
        }
    }

    private fun updateBtnOpenStatus() {
        binding.fabMore.let {
            if (isExpand) {
                it.setImageResource(R.drawable.charging_icon_more_open)
            } else {
                it.setImageResource(R.drawable.charging_icon_more_close)
            }
        }
    }

    fun setMapMode(isMapMode: Boolean) {
        binding.fabPositioning.isVisible = isMapMode
    }

    fun setToCharging(isCharging: Boolean) {
        isChargingStatus = isCharging
        binding.fabBase.let {
            if (isCharging) {
                it.setImageResource(R.drawable.charging_icon_oncharging)
            } else {
                it.setImageResource(R.drawable.charging_icon_qr)
            }
        }
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        hideButtons.forEach {
            it.setOnClickListener(listener)
        }
        binding.fabBase.setOnClickListener(listener)
    }

    companion object{
        const val ANIM_DURATION = 250L
    }
}