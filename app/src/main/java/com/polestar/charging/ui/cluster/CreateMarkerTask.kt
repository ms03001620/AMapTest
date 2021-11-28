package com.polestar.charging.ui.cluster

import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem

class CreateMarkerTask<T : ClusterItem>(
    val cluster: Cluster<T>,
    val newMarkers: MutableSet<MarkerWithPosition>,
    val animateFrom: LatLng?,
    val zoomCallback: RenderTask.OnZoomChange<T>
) {
    fun perform(markerModifier: MarkerModifier<T>) {
        if (cluster.size > 4) {
            cluster.items?.forEach { item ->
                zoomCallback.getMarkerCacheItem(item)?.let { marker ->
                    item.title?.let {
                        marker.title = it
                    }
                    item.snippet?.let {
                        marker.snippet = it
                    }
                    MarkerWithPosition(marker)
                } ?: run {
                    val options = MarkerOptions()
                        .position(animateFrom ?: item.position)
                    onBeforeClusterItemRendered(item, options)
                    val marker: Marker = zoomCallback.addMarker(options)
                    val markerWithPosition = MarkerWithPosition(marker)
                    zoomCallback.markerCachePut(item, marker)
                    if (animateFrom != null) {
                        markerModifier.animate(markerWithPosition, animateFrom, item.position);
                    }
                    markerWithPosition
                }.let {
                    newMarkers.add(it);
                }
            }
        } else {
            var marker: Marker? = zoomCallback.getClusterMarkerCache(cluster)
            marker?.let {
                it.title = "C:" + cluster.size
                val markerWithPosition = MarkerWithPosition(it);
                markerWithPosition
            } ?: run {
                val markerOptions = MarkerOptions()
                    .position(animateFrom ?: cluster.position)
                onBeforeClusterRendered(cluster, markerOptions)

                marker = zoomCallback.addClusterMarker(markerOptions)
                zoomCallback.clusterCachePut(cluster, marker!!)
                val markerWithPosition = MarkerWithPosition(marker!!)

                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.position!!)
                }
                markerWithPosition
            }.let {
                newMarkers.add(it);
            }
        }
    }

    fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions) {
        item.title?.let {
            markerOptions.title(it)
        }
        item.snippet?.let {
            markerOptions.snippet(it)
        }
    }

    fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        markerOptions.title("C:" + cluster.size)
    }
}

