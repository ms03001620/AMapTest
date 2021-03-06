package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng

interface Cluster<T : ClusterItem> {
    val position: LatLng
    val items: MutableCollection<T>
    val size: Int
}