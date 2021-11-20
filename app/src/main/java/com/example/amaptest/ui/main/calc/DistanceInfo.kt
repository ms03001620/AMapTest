package com.example.amaptest.ui.main.calc

data class DistanceInfo(val distanceMerge: Float, val enableCluster: Boolean)

fun DistanceInfo.same(
    target: DistanceInfo,
    deviation: Int = 5
): Boolean {
    return Math.abs(target.distanceMerge - distanceMerge) <= deviation
}