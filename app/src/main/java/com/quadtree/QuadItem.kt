package com.quadtree

class QuadItem<T : ClusterItem>(val clusterItem: T) : PointQuadTree.Item, Cluster<T> {
    override val point = PROJECTION.toPoint(clusterItem.position)
    override val position = clusterItem.position
    override val items = mutableListOf(clusterItem)
    override val size = 1

    override fun hashCode() = clusterItem.hashCode()

    override fun equals(other: Any?): Boolean {
        return if (other !is QuadItem<*>) {
            false
        } else other.clusterItem == clusterItem
    }

    companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}