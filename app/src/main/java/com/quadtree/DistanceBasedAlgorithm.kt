package com.quadtree


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

    /**
     * Adds an item to the algorithm
     *
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
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

    /**
     * Adds a collection of items to the algorithm
     *
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
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

    /**
     * Removes an item from the algorithm
     *
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
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

    /**
     * Removes a collection of items from the algorithm
     *
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    fun removeItems(items: Collection<T>): Boolean {
        var result = false
        synchronized(quadTree) {
            for (item in items) {
                // QuadItem delegates hashcode() and equals() to its item so,
                //   removing any QuadItem to that item will remove the item
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

    /**
     * Updates the provided item in the algorithm
     *
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    fun updateItem(item: T): Boolean {
        // TODO - Can this be optimized to update the item in-place if the location hasn't changed?
        var result: Boolean
        synchronized(quadTree) {
            result = removeItem(item)
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item)
            }
        }
        return result
    }

    fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()
        val zoomSpecificSpan = maxDistance / Math.pow(2.0, discreteZoom.toDouble()) / 256
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
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        val halfSpan = span / 2
        return Bounds(
            p.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan
        )
    }

    companion object {
        private const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 100 // essentially 100 dp.
    }
}