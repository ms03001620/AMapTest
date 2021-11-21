package com.quadtree

import com.amap.api.maps.model.LatLng

interface Cluster<T : ClusterItem?> {
    val position: LatLng?
    val items: Collection<T>?
    val size: Int
}