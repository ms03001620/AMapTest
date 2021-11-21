package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.ui.main.calc.Cluster
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.example.amaptest.ui.main.calc.RegionItem
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.ClusterItem
import com.quadtree.NonHierarchicalDistanceBasedAlgorithm

class ClusterCalcDemoV2 : ClusterCalcDemoBase {
    private val algorithm = NonHierarchicalDistanceBasedAlgorithm<ClusterItem>()

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

    class StationClusterItem(val stationDetail: StationDetail) : ClusterItem {
        override val position: LatLng
            get() = LatLng(stationDetail.lat ?: Double.NaN, stationDetail.lng ?: Double.NaN)
        override val title: String
            get() = stationDetail.stationid ?: "NoneId"
        override val snippet: String
            get() = stationDetail.providerName + stationDetail.stationName

    }
}