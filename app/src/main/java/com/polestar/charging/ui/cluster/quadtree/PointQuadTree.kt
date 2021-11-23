package com.polestar.charging.ui.cluster.quadtree

import com.polestar.charging.ui.cluster.base.Bounds
import com.polestar.charging.ui.cluster.base.Point
import java.util.ArrayList
import java.util.LinkedHashSet

/**
 * A quad tree which tracks items with a Point geometry.
 * See http://en.wikipedia.org/wiki/Quadtree for details on the data structure.
 * This class is not thread safe.
 */
class PointQuadTree<T : PointQuadTree.Item>(
    private val bounds: Bounds,
    private val depth: Int = 0
) {
    interface Item {
        val point: Point
    }

    private var mItems: MutableSet<T>? = null
    private var mChildren: MutableList<PointQuadTree<T>>? = null

    fun add(item: T) {
        if (bounds.contains(item.point.x, item.point.y)) {
            insert(item.point.x, item.point.y, item)
        }
    }

    private fun insert(x: Double, y: Double, item: T) {
        mChildren?.let {
            if (y < bounds.midY) {
                if (x < bounds.midX) {
                    // top left
                    it[0].insert(x, y, item)
                } else {
                    // top right
                    it[1].insert(x, y, item)
                }
            } else {
                if (x < bounds.midX) {
                    // bottom left
                    it[2].insert(x, y, item)
                } else {
                    it[3].insert(x, y, item)
                }
            }
        } ?: run {
            mItems = mItems ?: LinkedHashSet()
            mItems?.let {
                it.add(item)
                if (it.size > MAX_ELEMENTS && depth < MAX_DEPTH) {
                    split()
                }
            }
        }
    }

    private fun split() {
        mChildren = mutableListOf()
        mChildren?.add(
            PointQuadTree(
                Bounds(
                    bounds.minX,
                    bounds.midX,
                    bounds.minY,
                    bounds.midY
                ),
                depth + 1
            )
        )
        mChildren?.add(
            PointQuadTree(
                Bounds(
                    bounds.midX,
                    bounds.maxX,
                    bounds.minY,
                    bounds.midY
                ),
                depth + 1
            )
        )
        mChildren?.add(
            PointQuadTree(
                Bounds(
                    bounds.minX,
                    bounds.midX,
                    bounds.midY,
                    bounds.maxY
                ),
                depth + 1
            )
        )
        mChildren?.add(
            PointQuadTree(
                Bounds(
                    bounds.midX,
                    bounds.maxX,
                    bounds.midY,
                    bounds.maxY
                ),
                depth + 1
            )
        )
        val items = mItems
        mItems = null
        items?.forEach {
            insert(it.point.x, it.point.y, it)
        }
    }

    fun remove(item: T): Boolean {
        val point = item.point
        return if (bounds.contains(point.x, point.y)) {
            remove(point.x, point.y, item)
        } else {
            false
        }
    }

    private fun remove(x: Double, y: Double, item: T): Boolean {
        return mChildren?.let { children ->
            if (y < bounds.midY) {
                if (x < bounds.midX) {
                    children[0].remove(x, y, item)
                } else {
                    children[1].remove(x, y, item)
                }
            } else {
                if (x < bounds.midX) {
                    children[2].remove(x, y, item)
                } else {
                    children[3].remove(x, y, item)
                }
            }
        } ?: run {
            mItems?.remove(item) ?: false
        }
    }

    /**
     * Removes all points from the quadTree
     */
    fun clear() {
        mChildren = null
        mItems?.clear()
    }

    /**
     * Search for all items within a given bounds.
     */
    fun search(searchBounds: Bounds): Collection<T> {
        val results: MutableList<T> = ArrayList()
        search(searchBounds, results)
        return results
    }

    private fun search(searchBounds: Bounds, results: MutableCollection<T>) {
        if (bounds.intersects(searchBounds).not()) {
            return
        }
        mChildren?.let { children ->
            children.forEach {
                it.search(searchBounds, results)
            }
        } ?: run {
            mItems?.let {
                if (searchBounds.contains(bounds)) {
                    results.addAll(it)
                } else {
                    it.forEach {
                        if (searchBounds.contains(it.point)) {
                            results.add(it)
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Maximum number of elements to store in a quad before splitting.
         */
        private const val MAX_ELEMENTS = 50

        /**
         * Maximum depth.
         */
        private const val MAX_DEPTH = 40
    }
}