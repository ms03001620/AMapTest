package com.example.amaptest.marker

data class ClusterAnimData(
    val animTask: List<ClusterUtils.NodeTrackV2>,
    val deleteList: List<BaseMarkerData>,
    val zoom: Float
)