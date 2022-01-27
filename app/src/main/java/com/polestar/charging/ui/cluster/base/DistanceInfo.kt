package com.polestar.charging.ui.cluster.base

import kotlin.math.abs

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

fun DistanceInfo.sameZoom(
    target: DistanceInfo?,
    delta: Float = 0.3f
): Boolean {
    return abs((target?.zoomLevel ?: 0f) - zoomLevel) <= delta
}