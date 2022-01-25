package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import com.amap.api.maps.model.*
import com.example.amaptest.R
import com.polestar.repository.data.charging.showMarker

class MapProxy(private val map: BaseMap, context: Context) {
    private val iconSingle =
        IconGenerator(context, R.layout.charging_layout_marker_collapsed_v2, R.id.tv)
    private val iconCluster =
        IconGenerator(context, R.layout.charging_layout_marker_cluster_v2, R.id.text_cluster)

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

    fun getCollapsedBitmapDescriptor2(total: String): Bitmap? {
        return iconCluster.makeIcon(total)
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

    private fun createOptionsToPosition(
        baseMarkerData: BaseMarkerData,
        forceLatLng: LatLng? = null
    ): MarkerOptions {
        val createAtPosition = forceLatLng ?: baseMarkerData.getLatlng()
        return createMarkerOptions(baseMarkerData, createAtPosition)
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconSingle.makeIcon(total))
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconCluster.makeIcon(clusterSize.toString()))
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
            .setFlat(false)
            .infoWindowEnable(false)
}