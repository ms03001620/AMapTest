package com.polestar.customerservice.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.amaptest.databinding.LayoutFloatListBinding
import com.example.amaptest.floatlist.RepairStep
import com.example.amaptest.floatlist.RepairStepAdapter
import com.example.amaptest.floatlist.TimeLineDecoration

class FloatLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding: LayoutFloatListBinding
    private var isExpand = false
    private lateinit var adapter: RepairStepAdapter

    private var bgColor = Color.parseColor("#F9F9F9")
    //private var bgColor = Color.parseColor("#FF0000")

    init {
        binding = LayoutFloatListBinding.inflate(LayoutInflater.from(context), this, true)

        binding.layoutTitle.setOnClickListener {
            toggle()
        }
        binding.space.setOnClickListener {
            toggle()
        }
        binding.container.setOnClickListener {
            // ignore click
        }

        initList()
    }

    private fun initList(){
        adapter = RepairStepAdapter{

        }
        binding.list.addItemDecoration(TimeLineDecoration(context))
        binding.list.itemAnimator = null
        binding.list.adapter = adapter
    }

    private fun toggle() {
        isExpand = !isExpand
        binding.root.updateLayoutParams {
            width =
                if (isExpand) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
            height =
                if (isExpand) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        }

        if (isExpand) {
            setupExpandView()
        } else {
            setupCollapseView()
        }
    }

    private fun setupExpandView() {
        binding.list.isVisible = true
        binding.space.isVisible = true
        binding.container.setBackgroundColor(bgColor)
    }

    private fun setupCollapseView() {
        binding.list.isVisible = false
        binding.space.isVisible = false
        binding.container.setBackgroundColor(Color.TRANSPARENT)
    }

    fun setData(mockList: List<RepairStep>) {
        adapter.submitList(mockList/*.subList(0, 2)*/)
    }
}