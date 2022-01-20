package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import com.amap.api.maps.model.*
import com.polestar.repository.data.charging.showMarker

class MapProxy1(private val map: BaseMap, private val context: Context) {
    private val iconGenerator = IconGenerator(context)

    fun createMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        baseMarkerDataList.map {
            createOptionsToPosition(it)
        }.let {
            ArrayList<MarkerOptions>(it)
        }.let { options ->
            map.addMarkers(options)
        }
    }

    fun createMarker(baseMarkerData: BaseMarkerData, forceLatLng: LatLng? = null): Marker? {
        return createMarker(createOptionsToPosition(baseMarkerData, forceLatLng))
    }

    fun updateMarker(marker: Marker, baseMarkerData: BaseMarkerData) {
        map.updateMarker(marker, baseMarkerData.getId(), createBitmapDescriptor(baseMarkerData))
    }

    fun removeMarker(id: String) {
        map.removeMarker(id)
    }

    private fun createMarker(markerOptions: MarkerOptions): Marker? {
        return map.addMarker(markerOptions)
    }

    fun getCollapsedBitmapDescriptor2(total: String): Bitmap {
        val p = iconGenerator.makeIconCluster(total)
        return p
    }

    fun getMarker(id: String): Marker? {
        return map.getMarker(id)
    }

    fun getAllMarkers(): List<Marker> {
        return map.getAllMarkers()
    }

    fun clear() {
        map.clear()
    }

    fun removeMarkers(removeList: List<String>) {
        if (removeList.isNotEmpty()) {
            removeList.forEach {
                removeMarker(it)
            }
        }
    }

    private fun createOptionsToPosition(baseMarkerData: BaseMarkerData, forceLatLng: LatLng? = null): MarkerOptions {
        val createAtPosition = forceLatLng ?: baseMarkerData.getLatlng()
        return createMarkerOptions(baseMarkerData, createAtPosition)
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(total))
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIconCluster(clusterSize.toString()))
    }

    private fun createBitmapDescriptor(baseMarkerData: BaseMarkerData) =
        when (baseMarkerData) {
            is MarkerCluster -> getClusterBitmapDescriptor(baseMarkerData.getSize())
            is MarkerSingle -> getCollapsedBitmapDescriptor(baseMarkerData.stationDetail.showMarker())
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }

    private fun createMarkerOptions(baseMarkerData: BaseMarkerData, latLng: LatLng) =
        MarkerOptions()
            .title(baseMarkerData.getId())
            .position(latLng)
            .icon(createBitmapDescriptor(baseMarkerData))
            .setFlat(true)
            .infoWindowEnable(false)
}