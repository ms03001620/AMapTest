package com.com.polestar.carcontrol.views.car

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.amaptest.databinding.LayoutHotLevelBinding

class HotLevelLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding =
        LayoutHotLevelBinding.inflate(LayoutInflater.from(context), this, true)

    private val colorOn = Color.parseColor("#FFFFFF")
    private val colorOff = Color.parseColor("#000000")

    fun setLevel(level: Int) {
        binding.hot1.setColorFilter(colorOff)
        binding.hot2.setColorFilter(colorOff)
        binding.hot3.setColorFilter(colorOff)

        when (level) {
            1 -> {
                binding.hot1.setColorFilter(colorOn)
            }
            2 -> {
                binding.hot1.setColorFilter(colorOn)
                binding.hot2.setColorFilter(colorOn)
            }
            3 -> {
                binding.hot1.setColorFilter(colorOn)
                binding.hot2.setColorFilter(colorOn)
                binding.hot3.setColorFilter(colorOn)
            }
            else -> {
                binding.hot1.setColorFilter(colorOn)
                binding.hot2.setColorFilter(colorOn)
                binding.hot3.setColorFilter(colorOn)
            }
        }
    }
}