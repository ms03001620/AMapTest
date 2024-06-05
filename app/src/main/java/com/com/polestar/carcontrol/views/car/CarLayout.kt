package com.com.polestar.carcontrol.views.car

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.amaptest.R
import com.example.amaptest.databinding.LayoutCarHotLevelBinding

class CarLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding =
        LayoutCarHotLevelBinding.inflate(LayoutInflater.from(context), this, true)

    private var steering = false
    private var leftLevel = 0
    private var rightLevel = 0

    fun enableSteering(on: Boolean) {
        this.steering = on
        binding.steering.setBackgroundResource(
            if (on) {
                R.drawable.cc_ic_steering_wheel_on
            } else {
                R.drawable.cc_ic_steering_wheel_off
            }
        )
    }

    fun setLeftSeatLevel(level: Int) {
        this.leftLevel = level
        binding.levelLeft.setLevel(level)
    }

    fun setRightSeatLevel(level: Int) {
        this.rightLevel = level
        binding.levelRight.setLevel(level)
    }

    fun getSteering() = steering
    fun getLeftLevel() = leftLevel
    fun getRightLevel() = rightLevel
}