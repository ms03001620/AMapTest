package com.polestar.customerservice.widget.floatlist

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.amaptest.databinding.CsLayoutFloatListBinding
import com.example.amaptest.floatlist.RepairStep

class FloatListLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding: CsLayoutFloatListBinding
    private var isExpand = false
    private lateinit var adapter: RepairStepAdapter
    private var bgColor = Color.parseColor("#F9F9F9")

    init {
        binding = CsLayoutFloatListBinding.inflate(LayoutInflater.from(context), this, true)

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

    private fun initList() {
        adapter = RepairStepAdapter {}
        binding.list.addItemDecoration(TimeLineDecoration(context))
        binding.list.itemAnimator = null
        binding.list.adapter = adapter
    }

    private fun toggle() {
        isExpand = !isExpand
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
        binding.checkIcon.isChecked = true
        binding.statusTitle.setTextColor(Color.parseColor("#FF7500"))
        binding.root.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun setupCollapseView() {
        binding.list.isVisible = false
        binding.space.isVisible = false
        binding.container.setBackgroundColor(Color.TRANSPARENT)
        binding.checkIcon.isChecked = false
        binding.statusTitle.setTextColor(Color.parseColor("#101820"))
        binding.root.updateLayoutParams {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    fun setTitle(title: String?) {
        binding.statusTitle.text = title.orEmpty()
    }

    fun setData(mockList: List<RepairStep>) {
        adapter.submitList(mockList/*.subList(0, 2)*/)
    }
}