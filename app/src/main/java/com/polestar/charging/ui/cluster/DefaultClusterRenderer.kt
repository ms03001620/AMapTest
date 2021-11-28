package com.polestar.charging.ui.cluster

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.example.amaptest.R
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.Point
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.freeAcDcAll
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DefaultClusterRenderer<T : ClusterItem>(context: Context, val map: AMap) {
    val mDensity = context.resources.displayMetrics.density
    val mIconGenerator = IconGenerator(context)
    val mContainer = LayoutInflater.from(context)
        .inflate(R.layout.charging_layout_marker_collapsed, null) as ViewGroup
    val mAnimate = true
    var mZoom: Float = 0f
    val markerManager = MarkerManager(map)

    val mMarkers = markerManager.newCollection()

    var mMarkerWithPositions =
        Collections.newSetFromMap(ConcurrentHashMap<MarkerWithPosition, Boolean>());

    val mClusterMarkers = markerManager.newCollection()

    private val mMarkerCache = MarkerCache<T>()
    private val mClusterMarkerCache = MarkerCache<Cluster<T>>()

    /**
     * start
     */
    fun onClustersChanged(clusters: Set<Cluster<T>>) {
        RenderTask(
            mMarkerCache,
            mClusterMarkerCache,
            clusters,
            onZoomChange,
            map.cameraPosition.zoom
        ).run()
    }

    val onZoomChange = object : RenderTask.OnZoomChange<T> {
        override fun getZoom() = mZoom

        override fun setZoom(newZoom: Float) {
            mZoom = newZoom
        }

        override fun removeMarker(marker: Marker) {
            mMarkerCache.remove(marker)
            mClusterMarkerCache.remove(marker)
            markerManager.remove(marker)
        }

        override fun getMarkerCacheItem(cluster: T): Marker? {
            return mMarkerCache.get(cluster)
        }

        override fun addMarker(markerOptions: MarkerOptions): Marker {
            return mMarkers.addMarker(markerOptions)
        }

        override fun getMarker() = mMarkers

        override fun getMarkerWithPositions() = mMarkerWithPositions

        override fun setMarkerWithPositions(set: MutableSet<MarkerWithPosition>) {
            mMarkerWithPositions = set
        }

        override fun addClusterMarker(markerOptions: MarkerOptions): Marker {
            return mClusterMarkers.addMarker(markerOptions)
        }

        override fun markerCachePut(item: T, marker: Marker) {
            mMarkerCache.put(item, marker)
        }

        override fun getClusterMarkerCache(cluster: Cluster<T>): Marker? {
            return mClusterMarkerCache.get(cluster)
        }

        override fun clusterCachePut(cluster: Cluster<T>, marker: Marker) {
            mClusterMarkerCache.put(cluster, marker)
        }
    }

}