package com.polestar.charging.ui.cluster.quadtree

import com.polestar.charging.ui.cluster.base.Bounds
import com.polestar.charging.ui.cluster.base.Point
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class PointQuadTreeTest {
    private lateinit var tree: PointQuadTree<PointQuadTree.Item>

    @Before
    fun setUp() {
        tree = PointQuadTree(Bounds(0.0, 1.0, 0.0, 1.0))
    }

    @Test
    fun testAddOnePoint() {
        val item = Item(0.0, 0.0)
        tree.add(item)
        val items = searchAll()
        assertEquals(1, items.size.toLong())
        tree.clear()
    }

    @Test
    fun testEmpty() {
        assertEquals(0, searchAll().size)
    }

    @Test
    fun testMultiplePoints() {
        val item1 = Item(0.0, 0.0)
        val item2 = Item(.1, .1)
        val item3 = Item(.2, .2)

        assertFalse(tree.remove(item1))

        tree.add(item1)
        tree.add(item2)
        tree.add(item3)

        val items = searchAll()

        assertEquals(3, items.size)
        assertTrue(items.contains(item1))
        assertTrue(items.contains(item2))
        assertTrue(items.contains(item3))

        assertTrue(tree.remove(item1))
        assertTrue(tree.remove(item2))
        assertTrue(tree.remove(item3))

        assertEquals(0, searchAll().size)
        assertFalse(tree.remove(item1))
    }

    @Test
    fun testSameLocationDifferentPoint() {
        tree.add(Item(.0, .0))
        tree.add(Item(.0, .0))

        assertEquals(2, searchAll().size)
    }

    @Test
    fun testClear() {
        tree.add(Item(.0, .0))
        tree.add(Item(.0, .0))
        tree.add(Item(.1, .1))
        tree.add(Item(.2, .2))
        tree.add(Item(.3, .3))

        tree.clear()
        assertEquals(0, searchAll().size)
    }

    @Test
    fun testSearch() {
        for (i in 0..9999) {
            tree.add(Item(i / 20000.0, i / 20000.0))
        }

        assertEquals(10000, searchAll().size)
        assertEquals(1, tree.search(Bounds(0.0, 0.00001, 0.0, 0.00001)).size)
        assertEquals(0, tree.search(Bounds(.7, .8, .7, .8)).size)
    }

    @Test
    fun test4Point() {
        tree.add(Item(.2, .2))
        tree.add(Item(.7, .2))
        tree.add(Item(.2, .7))
        tree.add(Item(.7, .7))
        assertEquals(2, tree.search(Bounds(.0, .5, .0, 1.0)).size)
    }

    @Test
    fun testDeepTree() {
        for (i in 0..29999) {
            tree.add(Item(.0, .0))
        }
        assertEquals(30000, searchAll().size)
        assertEquals(30000, tree.search(Bounds(.0, .1, .0, .1)).size)
        assertEquals(0, tree.search(Bounds(.1, 1.0, .1, 1.0)).size)
    }

    @Test
    fun testManyPoints() {
        for (i in 0..199) {
            for (j in 0..1999) {
                tree.add(Item(i / 200.0, j / 2000.0))
            }
        }

        assertEquals(400000, searchAll().size)
        assertEquals(100000, tree.search(Bounds(.0, .5, .0, .5)).size)
        assertEquals(100000, tree.search(Bounds(.5, 1.0, .0, .5)).size)
        assertEquals(25000, tree.search(Bounds(.0, .25, .0, .25)).size)
        assertEquals(25000, tree.search(Bounds(.75, 1.0, .75, 1.0)).size)

        assertEquals(399800, tree.search(Bounds(.0, .999, .0, .999)).size)
        assertEquals(4221, tree.search(Bounds(.8, .9, .8, .9)).size)
        assertEquals(4200, tree.search(Bounds(.0, 1.0, .0, .01)).size)
        assertEquals(16441, tree.search(Bounds(.4, .6, .4, .6)).size)

        assertEquals(1, tree.search(Bounds(.0, .001, .0, .0001)).size)
        assertEquals(26617, tree.search(Bounds(.356, .574, .678, .987)).size)
        assertEquals(44689, tree.search(Bounds(.123, .456, .456, .789)).size)
        assertEquals(4906, tree.search(Bounds(.111, .222, .333, .444)).size)

        tree.clear()
        assertEquals(0, searchAll().size)
    }

    @Test
    fun testRandomPoints() {
        val random = Random
        for (i in 0..99999) {
            tree.add(Item(random.nextDouble(), random.nextDouble()))
        }
        searchAll()
        tree.search(Bounds(.0, .5, .0, .5))
        tree.search(Bounds(.0, .25, .0, .25))
        tree.search(Bounds(.0, .125, .0, .125))
        tree.search(Bounds(.0, .999, .0, .999))


        tree.search(Bounds(.0, 1.0, .0, .01))
        tree.search(Bounds(.4, .6, .4, .6))
        tree.search(Bounds(.356, .574, .678, .987))
        tree.search(Bounds(.123, .456, .456, .789))
        tree.search(Bounds(.111, .222, .333, .444))
    }


    private fun searchAll() = tree.search(Bounds(0.0, 1.0, 0.0, 1.0))

    private class Item(x: Double, y: Double) : PointQuadTree.Item {
        override val point: Point = Point(x, y)
    }
}