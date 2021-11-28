package com.polestar.charging.ui.cluster

import android.os.Looper
import com.amap.api.maps.Projection
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.Point
import com.polestar.charging.ui.cluster.base.SphericalMercatorProjection
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm.Companion.DEFAULT_MAX_DISTANCE_AT_ZOOM
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RenderTask<T : ClusterItem>(
    val mMarkerCache: MarkerCache<T>,
    val mClusterMarkerCache: MarkerCache<Cluster<T>>,
    val clusters: Set<Cluster<T>>,
    val zoomCallback: OnZoomChange<T>,
    val mMapZoom: Float
) {

    val mSphericalMercatorProjection: SphericalMercatorProjection

    init {
        val minZoom = Math.min(mMapZoom, zoomCallback.getZoom())
        mSphericalMercatorProjection =
            SphericalMercatorProjection(256 * Math.pow(2.0, minZoom.toDouble()))
    }

    interface OnZoomChange<T : ClusterItem> {
        fun getZoom(): Float
        fun setZoom(newZoom: Float)
        fun removeMarker(marker: Marker)
        fun getMarkerCacheItem(cluster: T): Marker?
        fun addMarker(markerOptions: MarkerOptions): Marker
        fun addClusterMarker(markerOptions: MarkerOptions): Marker
        fun markerCachePut(item: T, marker: Marker)
        fun getClusterMarkerCache(cluster: Cluster<T>): Marker?
        fun clusterCachePut(cluster: Cluster<T>, marker: Marker)
        fun getMarker(): MarkerManager.Collection
        fun getMarkerWithPositions(): MutableSet<MarkerWithPosition>
        fun setMarkerWithPositions(set: MutableSet<MarkerWithPosition>)
    }


    fun run() {
        val existingClustersOnScreen = clusters.filter {
            it.size > 4
        }.map { cluster ->
            cluster.position?.let {
                mSphericalMercatorProjection.toPoint(it)
            }
        }

        val markerModifier = MarkerModifier(mMarkerCache, mClusterMarkerCache, zoomCallback)

        // Create the new markers and animate them to their new positions.
        val newMarkers = Collections.newSetFromMap(ConcurrentHashMap<MarkerWithPosition, Boolean>())
        clusters.forEach { cluster ->
            if (zoomingIn()) {
                cluster.position?.let {
                    mSphericalMercatorProjection.toPoint(it)
                }.let {
                    findClosestCluster(existingClustersOnScreen, it)
                }?.let {
                    val animateTo = mSphericalMercatorProjection.toLatLng(it)
                    markerModifier.add(
                        true,
                        CreateMarkerTask(cluster, newMarkers, animateTo, zoomCallback)
                    );
                } ?: run {
                    markerModifier.add(
                        true,
                        CreateMarkerTask(cluster, newMarkers, null, zoomCallback)
                    )
                }
            } else {
                markerModifier.add(true, CreateMarkerTask(cluster, newMarkers, null, zoomCallback))
            }
        }

        for (i in 0..9) {
            markerModifier.performNextTask()
        }
        //markerModifier.waitUntilFree();

        Handler.postDelayed({
            val markersToRemove = zoomCallback.getMarkerWithPositions()
            markersToRemove.removeAll(newMarkers)

            val newClustersOnScreen = mutableListOf<Point>()

            clusters.filter {
                it.size > 4
            }.forEach {
                mSphericalMercatorProjection?.toPointOrNull(it.position)?.let {
                    newClustersOnScreen.add(it)
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            markersToRemove.forEach { markerPosition ->
                if (!zoomingIn() && zoomDelta() > -3) {
                    mSphericalMercatorProjection?.toPointOrNull(markerPosition.position)?.let {
                        findClosestCluster(newClustersOnScreen, it)?.let {
                            markerModifier.animateThenRemove(
                                markerPosition, markerPosition.position,
                                mSphericalMercatorProjection!!.toLatLng(it)
                            )
                        } ?: run {
                            markerModifier.remove(true, markerPosition.marker);
                        }
                    }
                } else {
                    markerModifier.remove(true, markerPosition.marker);
                }
            }

            for (i in 0..9) {
                markerModifier.performNextTask()
            }

        }, 3000)

        Handler.postDelayed({
            zoomCallback.setMarkerWithPositions(newMarkers)
            zoomCallback.setZoom(mMapZoom)
        }, 6000)
    }

    val Handler = android.os.Handler(Looper.getMainLooper())

    fun findClosestCluster(markers: List<Point?>?, point: Point?): Point? {
        var minDistSquared: Double =
            (DEFAULT_MAX_DISTANCE_AT_ZOOM * DEFAULT_MAX_DISTANCE_AT_ZOOM).toDouble()
        var result: Point? = null

        point?.let { pNotNull ->
            markers?.forEach { element ->
                element?.let { node ->
                    val dist = distanceSquared(node, pNotNull)
                    if (dist < minDistSquared) {
                        minDistSquared = dist
                        result = node
                    }
                }
            }
        }
        return result
    }


    fun zoomingIn() = mMapZoom > zoomCallback.getZoom()


    fun zoomDelta() = mMapZoom - zoomCallback.getZoom()

    private fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }
}