package com.example.amaptest.plate

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.amaptest.R

class PlateSelectorAdapter(private val onClick: (Plate) -> Unit) : RecyclerView.Adapter<PlateSelectorAdapter.PlateViewHolder>() {
    private var defaultVin: String? = null
    private var data = mutableListOf<Plate>()

    class PlateViewHolder(itemView: View, val onClick: (Plate) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val plateText: TextView = itemView.findViewById(R.id.text_plate)
        private val vinText: TextView = itemView.findViewById(R.id.text_vin)
        private val greenBg: ImageView = itemView.findViewById(R.id.bg_green)
        private val blueBg: ImageView = itemView.findViewById(R.id.bg_blue)
        private val radioBg: ImageView = itemView.findViewById(R.id.image_radio)
        private var currentPlate: Plate? = null

        init {
            itemView.setOnClickListener {
                currentPlate?.let {
                    onClick(it)
                }
            }
        }

        fun bind(plate: Plate, defaultVin: String?) {
            currentPlate = plate
            vinText.text = vinText.resources.getString(R.string.car_frame_number, plate.vin)
            plateText.text = formatPlate(plate) //警AB12345

            if (isEv(plate)) {
                plateText.setTextColor(Color.BLACK)
                greenBg.isVisible = true
                blueBg.isVisible = false
            } else {
                plateText.setTextColor(Color.WHITE)
                greenBg.isVisible = false
                blueBg.isVisible = true
            }

            if (isDefaultVin(plate, defaultVin)) {
                radioBg.setImageResource(R.drawable.ic_light)
            } else {
                radioBg.setImageResource(R.drawable.ic_radio)
            }
        }

        private fun formatPlate(plate: Plate): String{
            return if (plate.string.length > 2) {
                plate.string.substring(0, 2) + "·" + plate.string.substring(2, plate.string.length)
            } else {
                plate.string
            }
        }

        private fun isDefaultVin(plate: Plate, defaultVin: String?) = plate.vin == defaultVin

        private fun isEv(plate: Plate) = plate.string.length >= 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.charging_station_plate, parent, false)
        return PlateViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlateViewHolder, position: Int) {
        holder.bind(data.get(position), defaultVin)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePlateInfo(info: List<Plate>) {
        data.clear()
        data.addAll(ArrayList(info))
        defaultVin = info.firstOrNull()?.vin
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDefaultVin(defaultVin: String?) {
        this.defaultVin = defaultVin
        notifyDataSetChanged()
    }
}

