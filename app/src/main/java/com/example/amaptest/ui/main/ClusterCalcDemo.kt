package com.example.amaptest.ui.main

import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.Cluster
import com.example.amaptest.ui.main.calc.ClusterItem
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.example.amaptest.ui.main.calc.RegionItem
import com.polestar.repository.data.charging.StationDetail

class ClusterCalcDemo: ClusterCalcDemoBase {
    //https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/index.html?overview-summary.html

    private val mClusterItems = mutableListOf<ClusterItem<StationDetail>>()

    override fun setData(it: List<StationDetail>) {
        mClusterItems.clear()
        it.map {
            RegionItem(it)
        }.let {
            mClusterItems.addAll(it)
        }
    }

    override fun calc(
        distanceInfo: DistanceInfo,
        visibleBounds: LatLngBounds?,
        callback: (list: List<Cluster>) -> Unit
    ) {
        logd("calc distanceMerge:$distanceInfo")
        val newResult = mutableListOf<Cluster>()

        mClusterItems.filter {
            true
        }.filter {
            visibleBounds?.contains(it.getPosition()) ?: true
        }.forEach { clusterItem ->
            var g :Cluster? = null

            newResult.forEach lit@{
                val dd =
                    AMapUtils.calculateLineDistance(clusterItem.getPosition(), it.getCenterLatLng())

                logd("calc dd:$dd, distanceMerge:$distanceInfo")

                if (distanceInfo.enableCluster && dd <= distanceInfo.distanceMerge) {
                    g = it
                    return@lit
                }
            }

            if (g == null) {
                newResult.add(Cluster(clusterItem))
            } else {
                g?.addClusterItem(clusterItem)
            }


/*            getCluster(distanceMerge, clusterItem.getPosition(), newResult)?.addClusterItem(
                clusterItem
            ) ?: run {
                Cluster(clusterItem).let {
                    newResult.add(it)
                }
            }*/
        }
        callback.invoke(newResult.toList())
    }

    private fun getCluster(
        distanceMerge: Float,
        latLng: LatLng,
        clusters: MutableList<Cluster>
    ): Cluster? {
        return clusters.firstOrNull {
            AMapUtils.calculateLineDistance(latLng, it.getCenterLatLng()) < distanceMerge
        }
    }

}