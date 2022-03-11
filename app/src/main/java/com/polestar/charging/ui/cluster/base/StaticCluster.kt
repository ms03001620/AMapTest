package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng
import java.util.LinkedHashSet

/**
 * A cluster whose center is determined upon creation.
 */
class StaticCluster<T : ClusterItem>(override val position: LatLng) : Cluster<T> {
    private val clusterItems: MutableCollection<T> = LinkedHashSet()
    fun add(t: T): Boolean {
        return clusterItems.add(t)
    }

    fun remove(t: T): Boolean {
        return clusterItems.remove(t)
    }

    override val items: MutableCollection<T>
        get() = clusterItems
    override val size: Int
        get() = clusterItems.size

    override fun hashCode(): Int {
        return position.hashCode() + clusterItems.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is StaticCluster<*>) {
            false
        } else other.position == position && other.clusterItems == clusterItems
    }
}