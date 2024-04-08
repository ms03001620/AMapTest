package com.example.amaptest.floatlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.databinding.ItemRepiarStepBinding


class RepairStepAdapter(
    val callback: (RepairStep) -> Unit,
) : ListAdapter<RepairStep, RepairStepAdapter.ViewHolder>(RepairStepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRepiarStepBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            callback
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(
        private val binding: ItemRepiarStepBinding,
        private val callback: (RepairStep) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RepairStep) {
            binding.title.text = item.title
            binding.subTitle.text = item.subTitle
            binding.time.text = item.time
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