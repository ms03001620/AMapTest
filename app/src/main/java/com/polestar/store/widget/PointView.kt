package com.polestar.store.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.amaptest.databinding.CmPointBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.min

class PointView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    val binding = CmPointBinding.inflate(LayoutInflater.from(context), this, true)

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
            showAddPoint(point)

            delay(600)
            showTotalPoint(total)

            delay(600)
            showAlreadyGetPoint()
        }
    }

    private fun showTotalPoint(total: Int) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = false
        binding.point.text = "$total"
        binding.done.isVisible = false
        binding.add.isVisible = false
    }

    private fun showAddPoint(point: Int) {
        binding.layoutPoint.isVisible = true
        binding.icon.isVisible = false
        binding.point.isVisible = true
        binding.done.isVisible = false
        binding.add.text = "+$point"
        binding.add.isVisible = true
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