package com.polestar.charging.ui.cluster.distance

import com.polestar.charging.ui.cluster.base.BaseClusterAlgorithm
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm

class DistanceQuadTreeAlgorithm : BaseClusterAlgorithm {
    private val algorithm = DistanceBasedAlgorithm<ClusterItem>()

    override fun feed(it: List<ClusterItem>) {
        algorithm.addItems(it)
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        callback.invoke(algorithm.getClusters(distanceInfo.cameraPosition?.zoom ?: 0f))
    }
}