package com.polestar.charging.ui.cluster.base

data class DistanceInfo(
    val distanceMerge: Float,
    val enableCluster: Boolean,
    val zoomLevel: Float = 0f
)


fun DistanceInfo.same(
    target: DistanceInfo,
    deviation: Int = 5
): Boolean {
    return Math.abs(target.distanceMerge - distanceMerge) <= deviation
}