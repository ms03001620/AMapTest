package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLngBounds

import com.example.amaptest.ui.main.calc.DistanceInfo
import com.example.amaptest.ui.main.calc.RegionItem
import com.example.amaptest.ui.main.calc.StationClusterItem
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.ClusterItem
import com.quadtree.DistanceBasedAlgorithm
import java.util.HashSet

class DistanceQuadTreeAlgorithm : BaseClusterAlgorithm {
    private val algorithm = DistanceBasedAlgorithm<StationClusterItem>()

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
        callback: (list: Set<com.quadtree.Cluster<StationClusterItem>>) -> Unit
    ) {

        callback.invoke(algorithm.getClusters(distanceInfo.cameraPosition.zoom))
        return
/*        algorithm.getClusters(distanceInfo.cameraPosition.zoom).forEach {
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
        }*/
    }
}