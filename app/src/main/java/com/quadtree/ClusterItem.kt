package com.quadtree

import com.amap.api.maps.model.LatLng

/**
 * ClusterItem represents a marker on the map.
 */
interface ClusterItem {
    /**
     * The position of this marker. This must always return the same value.
     */
    val position: LatLng

    /**
     * The title of this marker.
     */
    val title: String?

    /**
     * The description of this marker.
     */
    val snippet: String?
}