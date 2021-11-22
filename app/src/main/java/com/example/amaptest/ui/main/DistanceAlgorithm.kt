package com.example.amaptest.ui.main

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.*
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.StaticCluster

class DistanceAlgorithm: BaseClusterAlgorithm {
    //https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/index.html?overview-summary.html

    private val mClusterItems = mutableListOf<StationClusterItem>()

    override fun setData(it: List<StationDetail>) {
        mClusterItems.clear()
        it.map {
            StationClusterItem(it)
        }.let {
            mClusterItems.addAll(it)
        }
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        visibleBounds: LatLngBounds?,
        callback: (list: Set<com.quadtree.Cluster<StationClusterItem>>) -> Unit
    ) {
        logd("calc distanceMerge:$distanceInfo")
        val newResult = hashSetOf<com.quadtree.Cluster<StationClusterItem>>()

        mClusterItems.filter {
            true
        }.filter {
            visibleBounds?.contains(it.position) ?: true
        }.forEach { clusterItem ->
            var g :com.quadtree.Cluster<StationClusterItem>? = null

            newResult.forEach lit@{
                val dd = AMapUtils.calculateLineDistance(clusterItem.position, it.position)
                logd("calc dd:$dd, distanceMerge:$distanceInfo")
                if (distanceInfo.enableCluster && dd <= distanceInfo.distanceMerge) {
                    g = it
                    return@lit
                }
            }

            if (g == null) {
                val staticCluster = StaticCluster<StationClusterItem>(clusterItem.position)
                staticCluster.add(clusterItem)
                newResult.add(staticCluster)
            } else {
                g?.items?.add(clusterItem)
            }
        }
        callback.invoke(newResult)
    }

    private fun getCluster(
        distanceMerge: Float,
        latLng: LatLng,
        clusters: MutableList<Cluster>
    ): Cluster? {
        return clusters.firstOrNull {
            AMapUtils.calculateLineDistance(latLng, it.getCenterLatLng()) < distanceMerge
        }
    }

}