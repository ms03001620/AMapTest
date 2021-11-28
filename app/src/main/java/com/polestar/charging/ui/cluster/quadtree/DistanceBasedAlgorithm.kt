package com.polestar.charging.ui.cluster.quadtree

import com.polestar.charging.ui.cluster.base.*
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashSet

class DistanceBasedAlgorithm<T : ClusterItem> {
    private val maxDistance = DEFAULT_MAX_DISTANCE_AT_ZOOM

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val quadList: MutableCollection<QuadItem<T>> = LinkedHashSet()

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private val quadTree = PointQuadTree<QuadItem<T>>(Bounds(.0, 1.0, .0, 1.0))

    fun addItem(item: T): Boolean {
        var result: Boolean
        val quadItem = QuadItem(item)
        synchronized(quadTree) {
            result = quadList.add(quadItem)
            if (result) {
                quadTree.add(quadItem)
            }
        }
        return result
    }

    fun addItems(items: Collection<T>): Boolean {
        var result = false
        for (item in items) {
            val individualResult = addItem(item)
            if (individualResult) {
                result = true
            }
        }
        return result
    }

    fun clearItems() {
        synchronized(quadTree) {
            quadList.clear()
            quadTree.clear()
        }
    }

    fun removeItem(item: T): Boolean {
        var result: Boolean
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        val quadItem = QuadItem(item)
        synchronized(quadTree) {
            result = quadList.remove(quadItem)
            if (result) {
                quadTree.remove(quadItem)
            }
        }
        return result
    }

    fun removeItems(items: Collection<T>): Boolean {
        var result = false
        synchronized(quadTree) {
            for (item in items) {
                val quadItem = QuadItem(item)
                val individualResult = quadList.remove(quadItem)
                if (individualResult) {
                    quadTree.remove(quadItem)
                    result = true
                }
            }
        }
        return result
    }

    fun updateItem(item: T): Boolean {
        var result: Boolean
        synchronized(quadTree) {
            result = removeItem(item)
            if (result) {
                result = addItem(item)
            }
        }
        return result
    }

    fun getClusters(zoom: Float): Set<Cluster<T>> {
        val zoomSpecificSpan = maxDistance / Math.pow(2.0, zoom.toDouble()) / 256
        val visitedCandidates: MutableSet<QuadItem<T>> = HashSet()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster: MutableMap<QuadItem<T>, Double> = HashMap()
        val itemToCluster: MutableMap<QuadItem<T>, StaticCluster<T>> = HashMap()
        synchronized(quadTree) {
            for (candidate in quadList) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }
                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val searchBoundsItems = quadTree.search(searchBounds)
                if (searchBoundsItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val staticCluster = StaticCluster<T>(candidate.clusterItem.position)
                results.add(staticCluster)
                for (clusterItem in searchBoundsItems) {
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    val existingDistance = distanceToCluster[clusterItem]
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]!!.remove(clusterItem.clusterItem)
                    }
                    distanceToCluster[clusterItem] = distance
                    staticCluster.add(clusterItem.clusterItem)
                    itemToCluster[clusterItem] = staticCluster
                }
                visitedCandidates.addAll(searchBoundsItems)
            }
        }
        return results
    }

    val items: Collection<T>
        get() {
            val items: MutableSet<T> = LinkedHashSet()
            synchronized(quadTree) {
                for (quadItem in quadList) {
                    items.add(quadItem.clusterItem)
                }
            }
            return items
        }

    private fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }

    private fun createBoundsFromSpan(p: Point, span: Double): Bounds {
        val halfSpan = span / 2
        return Bounds(
            p.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan
        )
    }

    companion object {
        const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 120 // essentially 100 dp.
    }
}