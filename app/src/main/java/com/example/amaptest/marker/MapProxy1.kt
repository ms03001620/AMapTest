package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.showMarker

class MapProxy1(private val map: AMap, private val context: Context) {
    private val iconGenerator = IconGenerator(context)

    fun createMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        baseMarkerDataList.map {
            createOptionsToPosition(it)
        }.let {
            ArrayList<MarkerOptions>(it)
        }.let {
            map.addMarkers(it, false/*boolean moveToCenter */)
        }
    }

    fun createMarker(baseMarkerData: BaseMarkerData, forceLatLng: LatLng? = null): Marker? {
        return createMarker(createOptionsToPosition(baseMarkerData, forceLatLng))
    }

    fun updateMarker(marker: Marker, baseMarkerData: BaseMarkerData) {
        marker.setIcon(createBitmapDescriptor(baseMarkerData))
    }

    fun removeMarker(marker: Marker) {
        marker.remove()
    }

    private fun createMarker(markerOptions: MarkerOptions): Marker? {
        return map.addMarker(markerOptions)
    }

    fun getCollapsedBitmapDescriptor2(total: String): Bitmap {
        val p = iconGenerator.makeIconCluster(total)
        return p
    }

    fun getMarker(latLng: LatLng?): Marker? {
        return map.mapScreenMarkers.firstOrNull { marker ->
            ClusterUtils.isSamePosition(latLng, marker.position)
        }
    }

    fun getMarker(baseMarkerData: BaseMarkerData): Marker? {
        return getMarker(baseMarkerData.getLatlng())
    }

    fun clear() {
        map.clear(true)
    }

    fun removeMarkers(removeList: List<LatLng>) {
        if (removeList.isNotEmpty()) {
            map.mapScreenMarkers.filter { marker ->
                removeList.firstOrNull { ClusterUtils.isSamePosition(it, marker.position) } != null
            }.forEach {
                it.remove()
            }
        }
    }

    private fun createOptionsToPosition(baseMarkerData: BaseMarkerData, forceLatLng: LatLng? = null): MarkerOptions {
        val createAtPosition = forceLatLng ?: baseMarkerData.getLatlng()
        val options = when (baseMarkerData) {
            is MarkerCluster -> {
                stationToClusterOptions(baseMarkerData.getSize(), createAtPosition)
            }
            is MarkerSingle -> {
                stationToMarkerOptions(baseMarkerData.stationDetail, createAtPosition)
            }
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }
        return options
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(total))
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIconCluster(clusterSize.toString()))
    }

    private fun createBitmapDescriptor(baseMarkerData: BaseMarkerData): BitmapDescriptor? {
        return when (baseMarkerData) {
            is MarkerCluster -> getClusterBitmapDescriptor(baseMarkerData.getSize())
            is MarkerSingle -> getCollapsedBitmapDescriptor(baseMarkerData.stationDetail.showMarker())
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }
    }

    private fun stationToClusterOptions(size: Int, latLng: LatLng?) =
        MarkerOptions()
            .position(latLng)
            .icon(getClusterBitmapDescriptor(size))
            .setFlat(true)
            .infoWindowEnable(false)

    private fun stationToMarkerOptions(station: StationDetail, latLng: LatLng? = null) =
        MarkerOptions()
            .position(latLng ?: LatLng(station.lat ?: Double.NaN, station.lng ?: Double.NaN))
            .icon(getCollapsedBitmapDescriptor(station.showMarker()))
            .setFlat(true)
            .infoWindowEnable(false)
}