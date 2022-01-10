package com.example.amaptest.marker

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.example.amaptest.R
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.showMarker
import java.lang.IllegalStateException

class MapProxy(private val map: AMap, private val context: Context) {
    private val set = HashMap<String, Marker>()

    fun createMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        baseMarkerDataList.forEach {
            createMarker(it, it.getLatlng())
        }
    }

    fun createMarker(baseMarkerData: BaseMarkerData, latLng: LatLng?): Marker {
        baseMarkerData.getId().let { id ->
            if (set.containsKey(id).not()) {
                val option = createMarkerOptions(baseMarkerData, latLng)
                val marker = createMarker(option)
                if (marker != null) {
                    set[id] = marker
                } else {
                    throw IllegalStateException("create marker failed")
                }
                return marker
            } else {
                throw IllegalStateException("111111")
            }
        }
    }

    fun createMarkerOptions(baseMarkerData: BaseMarkerData, latLng: LatLng?): MarkerOptions {
        val options = when (baseMarkerData) {
            is MarkerCluster -> {
                stationToClusterOptions(baseMarkerData.getSize(), latLng)
            }
            is MarkerSingle -> {
                stationToMarkerOptions(baseMarkerData.stationDetail, latLng)
            }
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }
        return options
    }

    fun updateMarker(marker: Marker, baseMarkerData: BaseMarkerData) {
        marker.setMarkerOptions(createMarkerOptions(baseMarkerData, marker.position))
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

    fun removeMarkers(remove: MutableList<BaseMarkerData>) {
        remove.forEach {
            set.remove(it.getId())?.remove()
        }
    }

    fun removeMarker(id: String?) {
        set.remove(id)?.remove()
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

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.charging_layout_marker_cluster, null, false)
        view.findViewById<TextView>(R.id.text_cluster).text = clusterSize.toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun stationToClusterOptions(size: Int, latLng: LatLng?) =
        MarkerOptions()
            .position(latLng)
            .icon(getClusterBitmapDescriptor(size))
            .infoWindowEnable(true)

    private fun stationToMarkerOptions(station: StationDetail, latLng: LatLng? = null) =
        MarkerOptions()
            .position(latLng ?: LatLng(station.lat ?: Double.NaN, station.lng ?: Double.NaN))
            .icon(getCollapsedBitmapDescriptor(station.showMarker()))
            .infoWindowEnable(true)

    fun getMarker(baseMarkerData: BaseMarkerData): Marker? {
        return set.getOrDefault(baseMarkerData.getId(), null)
    }

    fun clear() {
        set.clear()
        map.clear(true)
    }
}