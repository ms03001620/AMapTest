package com.example.amaptest.marker

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.example.amaptest.R
import com.polestar.repository.data.charging.StationDetail

class MapProxy(private val map: AMap, private val context: Context) {
    val set = HashMap<String, Marker>()

    fun addMarker(station: StationDetail): Marker? {
        val id = station.id!!
        if (set.containsKey(id).not()) {
            return addMarker(stationToMarkerOptions(station))?.also {
                set.put(id, it)
            }
        }
        return null
    }

    fun addCluster(id: String, size: Int, latLng: LatLng?): Marker? {
        latLng?.let { latLng ->
            if (set.containsKey(id).not()) {
                return addMarker(stationToClusterOptions(size, latLng))?.also {
                    set.put(id, it)
                }
            }
        }
        return null
    }

    fun deleteMarker(station: StationDetail) {
        deleteMarker(station.id)
    }

    fun deleteMarker(id: String?) {
        set.remove(id)?.let {
            it.remove()
        }
    }

    private fun addMarker(markerOptions: MarkerOptions): Marker? {
        return map.addMarker(markerOptions)
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.charging_layout_marker_collapsed, null, false)
        view.findViewById<TextView>(R.id.tv).text = total
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.charging_layout_marker_cluster, null, false)
        view.findViewById<TextView>(R.id.text_cluster).text = clusterSize.toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun stationToClusterOptions(size: Int, latLng: LatLng) = MarkerOptions()
        .position(latLng)
        .icon(getClusterBitmapDescriptor(size))
        .infoWindowEnable(true)


    private fun stationToMarkerOptions(station: StationDetail) = MarkerOptions()
        .position(LatLng(station.lat ?: Double.NaN, station.lng ?: Double.NaN))
        .icon(getCollapsedBitmapDescriptor((station.acTotal ?: 0 + station.dcTotal!!).toString()))
        .infoWindowEnable(true)

    fun getMarker(stationDetail: StationDetail): Marker? {
        return set.getOrDefault(stationDetail.id, null)
    }

    fun remove(marker: Marker) {


    }

}