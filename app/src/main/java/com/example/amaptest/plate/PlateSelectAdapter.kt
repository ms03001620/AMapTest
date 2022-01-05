package com.example.amaptest.plate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.R

class PlateSelectAdapter(private val onClick: (Plate) -> Unit) :
    ListAdapter<Plate, PlateSelectAdapter.PlateViewHolder>(FlowerDiffCallback) {
    class PlateViewHolder(itemView: View, val onClick: (Plate) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private var currentFlower: Plate? = null

        init {
            itemView.setOnClickListener {
                currentFlower?.let {
                    onClick(it)
                }
            }
        }

        fun bind(flower: Plate) {
            currentFlower = flower
            name.text = flower.string
        }
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