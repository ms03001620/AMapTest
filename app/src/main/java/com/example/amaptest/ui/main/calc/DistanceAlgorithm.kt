package com.example.amaptest.ui.main.calc

import com.amap.api.maps.AMapUtils
import com.example.amaptest.ui.main.quadtree.Cluster
import com.example.amaptest.ui.main.quadtree.ClusterItem
import com.example.amaptest.ui.main.quadtree.StaticCluster

class DistanceAlgorithm : BaseClusterAlgorithm {
    //https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/index.html?overview-summary.html

    private val mClusterItems = mutableListOf<ClusterItem>()

    override fun feed(it: List<ClusterItem>) {
        mClusterItems.clear()
        mClusterItems.addAll(it)
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        val newResult = hashSetOf<Cluster<ClusterItem>>()

        mClusterItems.forEach { clusterItem ->
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