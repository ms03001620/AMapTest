package com.example.amaptest.ui.main

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.quadtree.Cluster
import com.quadtree.ClusterItem
import com.quadtree.StaticCluster

class DistanceBaseAlgorithm {
    private val mClusterItems = mutableListOf<ClusterItem>()

    fun feed(it: List<ClusterItem>) {
        mClusterItems.clear()
        mClusterItems.addAll(it)
    }

    fun calc(
        distanceMerge: Float,
        enableCluster: Boolean,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        logd("calc distanceMerge:$distanceMerge")
        val newResult = hashSetOf<Cluster<ClusterItem>>()

        mClusterItems.forEach { clusterItem ->
            newResult.firstOrNull {enableCluster &&
                        AMapUtils.calculateLineDistance(
                            clusterItem.position,
                            it.position
                        ) <= distanceMerge
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