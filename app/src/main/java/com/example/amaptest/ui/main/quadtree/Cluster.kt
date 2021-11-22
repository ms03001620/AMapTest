package com.example.amaptest.ui.main.quadtree

import com.amap.api.maps.model.LatLng

public interface Cluster<T : ClusterItem?> {
    val position: LatLng?
    val items: MutableCollection<T>?
    val size: Int
}