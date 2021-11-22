package com.example.amaptest.ui.main

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.quadtree.Cluster
import com.quadtree.ClusterItem
import com.quadtree.StaticCluster

class DistanceAlgorithm : BaseClusterAlgorithm {
    //https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/index.html?overview-summary.html

    private val mClusterItems = mutableListOf<ClusterItem>()

    override fun feed(it: List<ClusterItem>) {
        mClusterItems.clear()
        mClusterItems.addAll(it)
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        visibleBounds: LatLngBounds?,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        logd("calc distanceMerge:$distanceInfo")
        val newResult = hashSetOf<Cluster<ClusterItem>>()

        mClusterItems.filter {
            true
        }.filter {
            visibleBounds?.contains(it.position) ?: true
        }.forEach { clusterItem ->
            newResult.firstOrNull {
                distanceInfo.enableCluster &&
                        AMapUtils.calculateLineDistance(
                            clusterItem.position,
                            it.position
                        ) <= distanceInfo.distanceMerge
            }?.let {
                it.items?.add(clusterItem)
            } ?: run {
                StaticCluster<ClusterItem>(clusterItem.position).let {
                    it.add(clusterItem)
                    newResult.add(it)
                }
            }
        }
        callback.invoke(newResult)
    }

}