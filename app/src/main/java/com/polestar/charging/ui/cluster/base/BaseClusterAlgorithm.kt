package com.polestar.charging.ui.cluster.base

interface BaseClusterAlgorithm {
    fun feed(it: List<ClusterItem>)

    fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    )
}