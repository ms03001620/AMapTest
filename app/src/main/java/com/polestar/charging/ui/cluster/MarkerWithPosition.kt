package com.polestar.charging.ui.cluster

import com.amap.api.maps.model.Marker

class MarkerWithPosition(val marker: Marker) {
    var position = marker.position

    override fun equals(other: Any?): Boolean {
        if (other is MarkerWithPosition) {
            return marker.equals(other.marker)
        }
        return false
    }

    override fun hashCode(): Int {
        return marker.hashCode()
    }
}