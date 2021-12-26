package com.example.amaptest.marker

import com.example.amaptest.logd
import com.polestar.charging.ui.cluster.base.*

class AlgorithmWallpaper(private val impl: BaseClusterAlgorithm) : BaseClusterAlgorithm {
    private var lastDistanceMerge: DistanceInfo? = null

    override fun feed(list: List<ClusterItem>) {
        logd("feed:${list.size}", "clusterEvent")
        impl.feed(list)
        // data change, must remove distance info
        lastDistanceMerge = null
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    ) {
        logd("distanceMerge zoom: ${distanceInfo.cameraPosition?.zoom}", "clusterEvent")
        if(lastDistanceMerge?.cameraPosition?.zoom == distanceInfo.cameraPosition?.zoom) {
            logd("distanceMerge: skip: $distanceInfo", "clusterEvent")
            return // skip same
        }

        val od = lastDistanceMerge?.cameraPosition?.zoom ?: 0f
        val no = distanceInfo.cameraPosition?.zoom?:0f
        val zoomOut = od < no

        lastDistanceMerge = distanceInfo

        val start = System.currentTimeMillis()
        impl.calc(distanceInfo, callback)
        logd(
            "calc: ${System.currentTimeMillis() - start}, ${impl.javaClass.simpleName}, zoomOut: $zoomOut",
            "clusterEvent"
        )
    }
}