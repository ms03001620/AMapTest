package com.polestar.charging.ui.station.plate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plate(val string: String, val vin: String): Parcelable