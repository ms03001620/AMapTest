package com.example.amaptest.floatlist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.databinding.CsItemRepiarStepBinding

class RepairStepAdapter(
    val callback: (RepairStep) -> Unit,
) : ListAdapter<RepairStep, RepairStepAdapter.ViewHolder>(RepairStepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CsItemRepiarStepBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            callback
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    inner class ViewHolder(
        private val binding: CsItemRepiarStepBinding,
        private val callback: (RepairStep) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RepairStep, position: Int) {
            binding.title.text = item.title
            binding.subTitle.text = item.subTitle
            binding.time.text = item.time

            if (position == 0) {
                binding.title.isVisible = false
                binding.subTitle.setTextColor(Color.parseColor("#101820"))
            } else {
                binding.title.isVisible = true
                binding.subTitle.setTextColor(Color.parseColor("#C9C9C9"))
            }
        }
    }

    private class RepairStepDiffCallback : DiffUtil.ItemCallback<RepairStep>() {
        override fun areItemsTheSame(
            oldItem: RepairStep,
            newItem: RepairStep
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: RepairStep,
            newItem: RepairStep
        ): Boolean {
            return oldItem.time == newItem.time
        }
    }
}