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

    fun createMarker(station: StationDetail): Marker? {
        val id = station.id!!
        if (set.containsKey(id).not()) {
            return createMarker(stationToMarkerOptions(station))?.also {
                set.put(id, it)
            }
        }
        return null
    }

    /**
     * latLng is start position
     */
    fun createMarker(station: StationDetail, latLng: LatLng): Marker? {
        val id = station.id!!
        if (set.containsKey(id).not()) {
            return createMarker(stationToMarkerOptions(station, latLng))?.also {
                set.put(id, it)
            }
        }
        return null
    }

    fun createOrUpdateCluster(id: String, size: Int, latLng: LatLng?): Marker? {
        latLng?.let { latLngNotNull ->
            if (set.containsKey(id).not()) {
                // add
                return createMarker(stationToClusterOptions(size, latLngNotNull))?.also {
                    set[id] = it
                }
            } else {
                // update
                set[id]?.setMarkerOptions(stationToClusterOptions(size, latLngNotNull))
                return null
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

    private fun createMarker(markerOptions: MarkerOptions): Marker? {
        return map.addMarker(markerOptions)
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.charging_layout_marker_collapsed, null, false)
        view.findViewById<TextView>(R.id.tv).text = total
        return BitmapDescriptorFactory.fromView(view)
    }

    fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.charging_layout_marker_cluster, null, false)
        view.findViewById<TextView>(R.id.text_cluster).text = clusterSize.toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    fun stationToClusterOptions(size: Int, latLng: LatLng) = MarkerOptions()
        .position(latLng)
        .icon(getClusterBitmapDescriptor(size))
        .infoWindowEnable(true)


    private fun stationToMarkerOptions(station: StationDetail) = MarkerOptions()
        .position(LatLng(station.lat ?: Double.NaN, station.lng ?: Double.NaN))
        .icon(getCollapsedBitmapDescriptor((station.acTotal ?: 0 + station.dcTotal!!).toString()))
        .infoWindowEnable(true)

    private fun stationToMarkerOptions(station: StationDetail, latLng: LatLng) = MarkerOptions()
        .position(latLng)
        .icon(getCollapsedBitmapDescriptor((station.acTotal ?: 0 + station.dcTotal!!).toString()))
        .infoWindowEnable(true)


    fun getMarker(id: String): Marker? {
        return set.getOrDefault(id, null)
    }

    fun getMarker(baseMarkerData: BaseMarkerData): Marker? {
        return set.getOrDefault(baseMarkerData.getId(), null)
    }

    fun clear() {
        set.clear()
        map.clear(true)
    }

}