package com.polestar.customerservice.ui.drive.map

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.example.amaptest.R
import com.polestar.base.ext.dp

class DriverMoveHelper(
    val map: AMap,
    val startLatLng: LatLng,
    val endLatLng: LatLng,
    val driverName: String? = null,
    val mapPadding: Int = 40.dp,
) {
    private val iconDriver by lazy {
        BitmapDescriptorFactory.fromResource(R.mipmap.base_map_deriver)
    }

    private var driverMarker: Marker? = null

    fun putDriver(currentLatLng: LatLng) {
        LatLngBounds.Builder().apply {
            this.include(startLatLng)
            this.include(endLatLng)
            this.include(currentLatLng)
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, mapPadding)
        }.let {
            map.animateCamera(it)
        }

        driverMarker?.remove()
        driverMarker = map.addMarker(createMarket(driverName, currentLatLng, iconDriver))
    }

    private fun createMarket(name: String?, pos: LatLng, icon: BitmapDescriptor) = MarkerOptions()
        .position(pos)
        .icon(icon)
        .anchor(0.5f, 0.5f)

}
