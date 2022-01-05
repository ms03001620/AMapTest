package com.example.amaptest.plate

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.R

class PlateSelectAdapter(private val onClick: (Plate) -> Unit) :
    ListAdapter<Plate, PlateSelectAdapter.PlateViewHolder>(FlowerDiffCallback) {
    class PlateViewHolder(itemView: View, val onClick: (Plate) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val plateText: TextView = itemView.findViewById(R.id.text_plate)
        private val vinText: TextView = itemView.findViewById(R.id.text_vin)
        private val greenBg: ImageView = itemView.findViewById(R.id.bg_green)
        private val blueBg: ImageView = itemView.findViewById(R.id.bg_blue)
        private var currentPlate: Plate? = null

        init {
            plateText.setOnClickListener {
                currentPlate?.let {
                    onClick(it)
                }
            }
        }

        fun bind(plate: Plate) {
            currentPlate = plate
            vinText.text = "车架号：${plate.vin}"
            plateText.text = plate.string

            if (isEv(plate)) {
                plateText.setTextColor(Color.BLACK)
                greenBg.isVisible = true
                blueBg.isVisible = false
            } else {
                plateText.setTextColor(Color.WHITE)
                greenBg.isVisible = false
                blueBg.isVisible = true
            }
        }

        private fun isEv(plate: Plate) = plate.string.length >= 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.charging_station_plate, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        val plate = getItem(position)
        holder.bind(plate)
    }
}

object FlowerDiffCallback : DiffUtil.ItemCallback<Plate>() {
    override fun areItemsTheSame(oldItem: Plate, newItem: Plate): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Plate, newItem: Plate): Boolean {
        return oldItem.vin == newItem.vin
    }
}