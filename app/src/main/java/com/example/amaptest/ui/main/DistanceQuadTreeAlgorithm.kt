package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.quadtree.Cluster
import com.quadtree.ClusterItem
import com.quadtree.DistanceBasedAlgorithm

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