package com.example.amaptest.marker

import java.lang.StringBuilder

data class ClusterAnimData(
    val animTask: List<ClusterUtils.NodeTrack>,
    val deleteList: List<BaseMarkerData>,
    val zoom: Float,
    val currentNode: MutableList<BaseMarkerData>
)

fun ClusterAnimData.getInfoString(): String {
    val sb = StringBuilder()
    sb.append("animTask size:")
    sb.append(animTask.size)
    sb.append(", ")
    sb.append("deleteList size:")
    sb.append(deleteList.size)
    sb.append(", ")
    sb.append("zoom:")
    sb.append(zoom)
    return sb.toString()
}

fun ClusterAnimData.isAnimTaskEmpty(): Boolean {
    return animTask.isEmpty()
}