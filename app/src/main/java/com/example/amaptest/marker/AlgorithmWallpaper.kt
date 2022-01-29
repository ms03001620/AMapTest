package com.example.amaptest.marker

import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.*

class AlgorithmWallpaper(private val impl: BaseClusterAlgorithm) : BaseClusterAlgorithm {
    override fun feed(list: List<ClusterItem>) {
        logd("feed:${list.size}", "clusterEvent")
        impl.feed(list)
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        impl.calc(distanceInfo, callback)

    }

    override fun isFeed() = impl.isFeed()
}