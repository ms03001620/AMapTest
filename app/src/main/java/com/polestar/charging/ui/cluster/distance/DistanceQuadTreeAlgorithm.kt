package com.polestar.charging.ui.cluster.distance

import com.polestar.charging.ui.cluster.base.BaseClusterAlgorithm
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm

class DistanceQuadTreeAlgorithm : BaseClusterAlgorithm {
    private val algorithm = DistanceBasedAlgorithm<ClusterItem>()

    var feed:Boolean = false

    override fun feed(list: List<ClusterItem>) {
        algorithm.addItems(list)
        feed = true
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        callback.invoke(algorithm.getClusters(distanceInfo.zoomLevel))
    }

    override fun isFeed() = feed
}