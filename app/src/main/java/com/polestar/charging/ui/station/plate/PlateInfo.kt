package com.polestar.charging.ui.station.plate
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlateInfo(val defaultVin: String?, val plates: List<Plate>): Parcelable