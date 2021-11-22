package com.example.amaptest.ui.main

import com.example.amaptest.ui.main.calc.DistanceInfo
import com.quadtree.Cluster
import com.quadtree.ClusterItem

interface BaseClusterAlgorithm {
    fun feed(it: List<ClusterItem>)

    fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    )
}