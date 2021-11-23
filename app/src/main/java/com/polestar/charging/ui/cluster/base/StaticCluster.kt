package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng
import java.util.LinkedHashSet

/**
 * A cluster whose center is determined upon creation.
 */
class StaticCluster<T : ClusterItem?>(override val position: LatLng) : Cluster<T> {
    private val mItems: MutableCollection<T> = LinkedHashSet()
    fun add(t: T): Boolean {
        return mItems.add(t)
    }

    fun remove(t: T): Boolean {
        return mItems.remove(t)
    }

    override val items: MutableCollection<T>
        get() = mItems
    override val size: Int
        get() = mItems.size

    override fun hashCode(): Int {
        return position.hashCode() + mItems.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is StaticCluster<*>) {
            false
        } else other.position == position && other.mItems == mItems
    }
}