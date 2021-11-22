package com.example.amaptest.ui.main.calc

import com.example.amaptest.ui.main.quadtree.Cluster
import com.example.amaptest.ui.main.quadtree.ClusterItem
import com.example.amaptest.ui.main.quadtree.DistanceBasedAlgorithm

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