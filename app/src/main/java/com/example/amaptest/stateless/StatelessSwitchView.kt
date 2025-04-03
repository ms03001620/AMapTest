package com.example.amaptest.stateless // 替换为你的包名

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View // 导入 View 类
import android.widget.FrameLayout
import com.example.amaptest.databinding.ViewStatelessSwitchBinding
import com.google.android.material.switchmaterial.SwitchMaterial


class StatelessSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewStatelessSwitchBinding
    private val internalSwitch: SwitchMaterial
    private val clickOverlay: View // 覆盖层的引用

    // 回调 Lambda，用于通知外部消费者用户的交互意图
    var onCheckedChange: ((Boolean) -> Unit)? = null

    // 存储来自外部的、被认可的最新状态（"事实来源"状态）
    private var currentAuthoredState: Boolean = false

    init {
        // 使用视图绑定加载布局
        binding = ViewStatelessSwitchBinding.inflate(LayoutInflater.from(context), this)
        internalSwitch = binding.internalSwitch
        clickOverlay = binding.clickOverlay

        // --- 在覆盖层上设置点击监听器 ---
        clickOverlay.setOnClickListener {
            // 计算用户想要切换到的目标状态
            val intendedState = !currentAuthoredState
            // 将这个“意图”通过回调通知外部监听器
            onCheckedChange?.invoke(intendedState)
            // --- 关键：此时绝对不能改变 internalSwitch 的状态！---
        }

        // 初始设置：确保内部开关状态与初始认可状态一致
        internalSwitch.isChecked = currentAuthoredState
        // 可选：处理来自 XML 的自定义属性...
    }

    /**
     * 设置内部 Switch 的选中状态，此操作应由外部状态所有者驱动。
     * 这是改变视觉状态的唯一途径。
     *
     * @param checked 权威的选中状态。
     * @param animate 控制状态改变是否需要动画。默认为 true。初始设置时应设为 false。
     */
    @JvmOverloads
    fun setChecked(checked: Boolean, animate: Boolean = true) {
        // 存储新的权威状态
        currentAuthoredState = checked

        // 如果视觉上已经是正确的状态，则无需更新，避免不必要的操作
        if (internalSwitch.isChecked == checked) {
            return
        }

        // 更新内部开关的视觉状态
        internalSwitch.isChecked = checked

        // 根据 animate 标志处理动画
        if (!animate) {
            // 如果不需要动画，则立即跳转到最终绘制状态
            internalSwitch.jumpDrawablesToCurrentState()
        }
    }

    /**
     * 获取最后一次从外部设置的权威选中状态。
     */
    fun isChecked(): Boolean {
        // 返回我们存储的权威状态，因为这才是逻辑上的“真实”状态
        return currentAuthoredState
    }

    /**
     * 设置启用/禁用状态，需要同时影响视觉开关和点击覆盖层。
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        // 同时更新内部开关（视觉变灰）和覆盖层（阻止点击）的启用状态
        internalSwitch.isEnabled = enabled
        clickOverlay.isEnabled = enabled
    }

    // --- 可选：如果需要，可以代理 Switch 的其他相关方法 ---
    fun setText(text: CharSequence?) {
        internalSwitch.text = text
    }

    fun getText(): CharSequence? {
        return internalSwitch.text
    }

    // --- 无障碍功能 (基础示例) ---
    // 考虑根据 currentAuthoredState 设置内容描述和状态描述。
    // 可能需要在覆盖层上使用更复杂的 AccessibilityDelegate。
    fun setSwitchContentDescription(description: CharSequence?) {
        clickOverlay.contentDescription = description
        // 也可以设置状态描述：
        // ViewCompat.setStateDescription(clickOverlay, if (currentAuthoredState) "开" else "关")
    }
}