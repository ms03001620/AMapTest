package com.example.amaptest.carctrl

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.amaptest.R
import com.example.amaptest.databinding.LayoutCarItemStatusBinding

class CarItemStatusView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding = LayoutCarItemStatusBinding.inflate(LayoutInflater.from(context), this, true)
    private var name: CharSequence? = null
    private var status: ItemStatus = ItemStatus.LOADING
    private var icon: Drawable?=null
    private var bg: Drawable?=null

    init {
        initXmlValue(attrs)
        initBg()
        updateUi(CarItemState(name, status))
    }

    private fun initXmlValue(attrs: AttributeSet?) {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CsItemValueLayout)
        typedArray.getString(R.styleable.CsItemValueLayout_car_item_name).let {
            name = it
        }
        typedArray.getString(R.styleable.CsItemValueLayout_car_item_status).let {
            this.status = ItemStatus.pairStatusNameOrLoading(it)
        }
        typedArray.getDrawable(R.styleable.CsItemValueLayout_car_icon_sel).let {
            this.icon = it
        }
        typedArray.getDrawable(R.styleable.CsItemValueLayout_car_bg_color).let {
            this.bg = it
        }
        typedArray.recycle()
    }

    private fun initBg() {
        binding.icon.setImageDrawable(icon)
    }

    private fun updateUi(ui: CarItemState) {
        binding.name.text = ui.name
        binding.status.text = ui.status.label

        if (ui.status == ItemStatus.ON) {
            binding.icon.isSelected = true
            binding.name.setTextColor(Color.WHITE)
            binding.status.setTextColor(Color.WHITE)
            binding.root.setBackgroundColor(Color.parseColor("#FF7500"))
        }

        if (ui.status == ItemStatus.OFF) {
            binding.icon.isSelected = false
            binding.name.setTextColor(Color.parseColor("#101820"))
            binding.status.setTextColor(Color.parseColor("#707479"))
            binding.root.setBackgroundColor(Color.WHITE)
        }
    }

    data class CarItemState(
        val name: CharSequence? = null,
        val status: ItemStatus,
    )


    enum class ItemStatus(val label: String) {
        LOADING("加载中"), ON("开着"), OFF("关了");

        companion object{
            fun pairStatusOrLoading(label: String?) =
                ItemStatus.values().find { it.label == label } ?: LOADING

            fun pairStatusNameOrLoading(name: String?) =
                ItemStatus.values().find { it.name == name } ?: LOADING
        }
    }
}