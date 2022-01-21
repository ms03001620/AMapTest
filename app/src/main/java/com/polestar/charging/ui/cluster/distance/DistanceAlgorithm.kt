package com.polestar.charging.ui.cluster.distance

import com.amap.api.maps.AMapUtils
import com.polestar.charging.ui.cluster.base.BaseClusterAlgorithm
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StaticCluster

class DistanceAlgorithm : BaseClusterAlgorithm {
    //https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/index.html?overview-summary.html

    private val clusterItems = mutableListOf<ClusterItem>()

    override fun feed(list: List<ClusterItem>) {
        clusterItems.clear()
        clusterItems.addAll(list)
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        val newResult = hashSetOf<Cluster<ClusterItem>>()

        clusterItems.forEach { clusterItem ->
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

    override fun isFeed() = clusterItems.isNotEmpty()
}