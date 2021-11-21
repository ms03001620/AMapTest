package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.CameraPosition

data class DistanceInfo(
    val distanceMerge: Float,
    val enableCluster: Boolean,
    val cameraPosition: CameraPosition
)


fun DistanceInfo.same(
    target: DistanceInfo,
    deviation: Int = 5
): Boolean {
    return Math.abs(target.distanceMerge - distanceMerge) <= deviation
}