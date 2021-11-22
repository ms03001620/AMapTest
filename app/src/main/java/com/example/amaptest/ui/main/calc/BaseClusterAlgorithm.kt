package com.example.amaptest.ui.main.calc

import com.example.amaptest.ui.main.quadtree.Cluster
import com.example.amaptest.ui.main.quadtree.ClusterItem

interface BaseClusterAlgorithm {
    fun feed(it: List<ClusterItem>)

    fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    )
}