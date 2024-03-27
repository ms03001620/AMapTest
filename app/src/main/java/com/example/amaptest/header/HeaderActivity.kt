package com.example.amaptest.header

import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityHeaderBinding
import com.polestar.base.utils.StatusBarUtils
import com.polestar.base.utils.logd
import kotlin.math.roundToInt

class HeaderActivity : AppCompatActivity() {
    private val headerViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[HeaderVisibleViewModel::class.java]
    }

    val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityHeaderBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransparentStatusBar()
        setContentView(binding.root)

        binding.rootScroll.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
                headerViewModel.calcHeaderVisible(binding.imageRepair, binding.toolBar.height)
            })
        initObserver()
    }

    private fun initObserver(){
        headerViewModel.headerVisibleLiveData.observe(this) { visible ->
            // toolbar背景渐变
            val bgColor: Int = Color.WHITE
            val bg = Color.argb(
                (Color.alpha(bgColor) * visible).roundToInt(),
                Color.red(bgColor),
                Color.green(bgColor),
                Color.blue(bgColor)
            )
            binding.toolBar.setBackgroundColor(bg)

            // toolbar返回键颜色渐变
            val backgroundColor = ColorUtils.blendARGB(
                Color.parseColor("#FFFFFF"),//#FFFFFF
                Color.parseColor("#000000"),
                visible
            )
            binding.ivClose.setColorFilter(backgroundColor)
            // 系统状态栏图标颜色改变
            setStatusBar(isDark = visible > 0.5)
        }
    }

    private fun setTransparentStatusBar() {
        StatusBarUtils.setImmersionBar(
            this,
            Color.TRANSPARENT,
            hideStatusBar = false,
            isDark = true
        )
    }


    protected open fun setStatusBar(
        @ColorInt color: Int = Color.TRANSPARENT,
        hideStatusBar: Boolean = false,
        isDark: Boolean = true
    ) {
        StatusBarUtils.setImmersionBar(
            this,
            color,
            hideStatusBar,
            isDark
        )
    }
}