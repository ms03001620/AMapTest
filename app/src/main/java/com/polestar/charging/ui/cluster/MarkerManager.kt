package com.polestar.charging.ui.cluster

import com.amap.api.maps.AMap
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import java.util.HashMap
import java.util.LinkedHashSet

class MarkerManager(
    private val map: AMap
) {
    private val mAllObjects: MutableMap<Marker, Collection> = HashMap()

    fun newCollection(): Collection {
        return Collection()
    }

    fun remove(marker: Marker): Boolean {
        val collection = mAllObjects[marker]
        return collection != null && collection.remove(marker)
    }

    inner class Collection {
        private val mObjects: MutableSet<Marker> = LinkedHashSet()
        fun remove(marker: Marker): Boolean {
            if (mObjects.remove(marker)) {
                mAllObjects.remove(marker)
                marker.remove()
                return true
            }
            return false
        }

        fun addMarker(opts: MarkerOptions): Marker {
            val marker = map.addMarker(opts)
            mObjects.add(marker)
            mAllObjects[marker] = this
            return marker
        }
    }
}