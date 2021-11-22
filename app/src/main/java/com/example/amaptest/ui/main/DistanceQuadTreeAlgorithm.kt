package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.ui.main.calc.Cluster
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.example.amaptest.ui.main.calc.RegionItem
import com.example.amaptest.ui.main.calc.StationClusterItem
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.ClusterItem
import com.quadtree.DistanceBasedAlgorithm

class DistanceQuadTreeAlgorithm : BaseClusterAlgorithm {
    private val algorithm = DistanceBasedAlgorithm<ClusterItem>()

    override fun setData(it: List<StationDetail>) {
        it.map {
            StationClusterItem(it)
        }.let {
            algorithm.addItems(it)
        }
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        visibleBounds: LatLngBounds?,
        callback: (list: List<Cluster>) -> Unit
    ) {
        val resultList = mutableListOf<Cluster>()
        algorithm.getClusters(distanceInfo.cameraPosition.zoom).forEach {
            var cluster: Cluster? = null
            it.items?.forEachIndexed { index, clusterItem ->
                val t = RegionItem((clusterItem as StationClusterItem).stationDetail)
                if (index == 0) {
                    cluster = Cluster(t)
                } else {
                    cluster?.addClusterItem(t)
                }
                cluster?.let {
                    resultList.add(it)
                }
            }
        }.let {
            callback.invoke(resultList)
        }
    }
}