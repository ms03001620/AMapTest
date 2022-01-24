package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLngBounds

data class DistanceInfo(
    val distanceMerge: Float,
    val enableCluster: Boolean,
    val cameraPosition: CameraPosition? = null,
)


fun DistanceInfo.same(
    target: DistanceInfo,
    deviation: Int = 5
): Boolean {
    return Math.abs(target.distanceMerge - distanceMerge) <= deviation
}