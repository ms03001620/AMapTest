package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail

class Cluster(val clusterItem: ClusterItem<StationDetail>) {
    private val items = mutableListOf<ClusterItem<StationDetail>>()

    fun getCenterLatLng(): LatLng {
        return clusterItem.getPosition()
    }

    fun addClusterItem(clusterItem: ClusterItem<StationDetail>) {
        items.add(clusterItem)
    }

    fun isOnlyOne() = items.size == 1

    fun size() = items.size
}