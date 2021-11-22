package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLngBounds
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.Cluster
import com.quadtree.ClusterItem

interface BaseClusterAlgorithm {
    fun setData(it: List<StationDetail>)
    fun calc(
        distanceInfo: DistanceInfo,
        visibleBounds: LatLngBounds? = null,
        callback: (list: Set<Cluster<ClusterItem>>) -> Unit
    )
}