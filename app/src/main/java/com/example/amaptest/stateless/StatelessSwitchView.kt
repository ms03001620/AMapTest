package com.example.amaptest.stateless // 替换为你的包名

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.amaptest.databinding.ViewStatelessSwitchBinding

class StatelessSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewStatelessSwitchBinding =
        ViewStatelessSwitchBinding.inflate(LayoutInflater.from(context), this)

    var onCheckedChange: ((Boolean) -> Unit)? = null

    // 存储来自外部的权威状态
    private var currentAuthoredState: Boolean = false


    init {
        // 初始设置
        binding.internalSwitch.isChecked = currentAuthoredState


        binding.root.setOnClickListener {
            onCheckedChange?.invoke(!currentAuthoredState)
        }
    }



    /**
     * 设置权威状态，由外部调用。
     * (与之前版本相同)
     */
    @JvmOverloads
    fun setChecked(checked: Boolean, animate: Boolean = true) {
        currentAuthoredState = checked
        if (binding.internalSwitch.isChecked == checked) {
            return
        }
        binding.internalSwitch.isChecked = checked
        if (!animate) {
            binding.internalSwitch.jumpDrawablesToCurrentState()
        }
    }

    fun isChecked(): Boolean {
        return currentAuthoredState
    }


}