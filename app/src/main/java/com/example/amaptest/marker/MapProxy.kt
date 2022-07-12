package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import com.amap.api.maps.model.*
import com.example.amaptest.R
import com.polestar.base.utils.logd
import com.polestar.repository.data.charging.showMarker

class MapProxy(private val map: BaseMap, val context: Context) {
    private val iconSingle =
        IconGenerator(
            context,
            R.layout.charging_layout_marker_collapsed_v3,
            Color.WHITE,
            offsetHeight4Text = -10
        )
    private val iconCluster =
        IconGenerator(
            context,
            R.layout.charging_layout_marker_cluster_v3,
            Color.BLACK,
            offsetHeight4Text = 0
        )

    val mIconGenerator = com.polestar.charging.ui.cluster.ui.IconGenerator(context)

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
        if (ClusterUtils.isSamePosition(marker.position, baseMarkerData.getLatlng()).not()) {
            marker.position = baseMarkerData.getLatlng()
        }
        map.updateMarker(marker, baseMarkerData, createBitmapDescriptor(baseMarkerData))
    }

    fun updateMarkers(data: List<ClusterUpdateData>?) {
        data?.forEach { element ->
            getMarker(element.markerId)?.let {
                updateMarker(it, element.updatedData)
            } ?: run {
                throw IllegalArgumentException("marker not found")
            }
        }
    }

    fun removeClusters(data: List<BaseMarkerData>?) {
        data?.forEach {
            map.removeMarker(it.getId())
        }
    }

    fun removeMarker(id: String) {
        map.removeMarker(id)
    }

    fun createMarker(markerOptions: MarkerOptions): Marker? {
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

    fun clearMarker(keepIds: List<String>) {
        map.clearMarker(keepIds)
    }

    private fun createOptionsToPosition(
        baseMarkerData: BaseMarkerData,
        forceLatLng: LatLng? = null
    ): MarkerOptions {
        val createAtPosition = forceLatLng ?: baseMarkerData.getLatlng()
        return createMarkerOptions(baseMarkerData, createAtPosition)
    }

/*    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
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
    }*/

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconSingle.makeIcon(total))
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconCluster.makeIcon(clusterSize.toString()))
    }

    private fun createBitmapDescriptorBg(baseMarkerData: BaseMarkerData) =
        when (baseMarkerData) {
            is MarkerCluster -> getClusterBitmapDescriptor(baseMarkerData.getSize())
            is MarkerSingle -> getCollapsedBitmapDescriptor(baseMarkerData.stationDetail.showMarker())
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }

    private fun createBitmapDescriptor(baseMarkerData: BaseMarkerData) =
        mIconGenerator.getDescriptorForCluster(baseMarkerData.getSize())


    private fun createMarkerOptions(baseMarkerData: BaseMarkerData, latLng: LatLng) =
        MarkerOptions()
            .snippet(baseMarkerData.getSize().toString())
            .title(baseMarkerData.getId())
            .position(latLng)
            .icon(createBitmapDescriptor(baseMarkerData))
            .setFlat(false)
            .infoWindowEnable(false)
}