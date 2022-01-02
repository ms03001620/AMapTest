package com.polestar.charging.ui.cluster.quadtree

import com.amap.api.maps.model.LatLng
import com.example.amaptest.marker.JsonTestUtil
import com.polestar.charging.ui.cluster.base.ClusterItem
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class DistanceBasedAlgorithmTest {

    @Test
    fun baseBigTest() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations570.json")
        assertEquals(570, list.size)

        val algorithm = DistanceBasedAlgorithm<ClusterItem>()

        assertTrue(algorithm.addItems(list))

        val c = algorithm.getClusters(8.0f)

        assertEquals(1, c.size)
        assertEquals(570, c.toList()[0].items?.size)

        assertEquals(5, algorithm.getClusters(9.0f).size)
    }

    @Test
    fun baseTest() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations.json")
        assertEquals(20, list.size)

        val algorithm = DistanceBasedAlgorithm<ClusterItem>()

        assertTrue(algorithm.addItems(list))

        assertEquals(1, algorithm.getClusters(11f).size)
        assertEquals(1, algorithm.getClusters(12f).size)
        assertEquals(2, algorithm.getClusters(13f).size)
        assertEquals(7, algorithm.getClusters(14f).size)
    }

    @Test
    fun testAddRemoveUpdateClear() {
        val item_1_5: ClusterItem =
            JsonTestUtil.TestClusterItem("", LatLng(0.1, 0.5), "title1", "")
        val item_2_3: ClusterItem =
            JsonTestUtil.TestClusterItem("",LatLng(0.2, 0.3), "title2", "")
        val algo: DistanceBasedAlgorithm<ClusterItem> =
            DistanceBasedAlgorithm()
        assertTrue(algo.addItem(item_1_5))
        assertTrue(algo.addItem(item_2_3))
        assertEquals(2, algo.items.size)
        assertTrue(algo.removeItem(item_1_5))
        assertEquals(1, algo.items.size)
        Assert.assertFalse(algo.items.contains(item_1_5))
        assertTrue(algo.items.contains(item_2_3))

        // Update the item still in the algorithm
        (item_2_3 as JsonTestUtil.TestClusterItem).title = "newTitle"
        assertTrue(algo.updateItem(item_2_3))

        // Try to remove the item that was already removed
        Assert.assertFalse(algo.removeItem(item_1_5))

        // Try to update the item that was already removed
        Assert.assertFalse(algo.updateItem(item_1_5))
        algo.clearItems()
        assertEquals(0, algo.items.size)

        // Test bulk operations
        val items = Arrays.asList(item_1_5, item_2_3)
        assertTrue(algo.addItems(items))

        // Try to bulk add items that were already added
        Assert.assertFalse(algo.addItems(items))
        assertTrue(algo.removeItems(items))

        // Try to bulk remove items that were already removed
        Assert.assertFalse(algo.removeItems(items))
    }

    @Test
    fun testInsertionOrder() {
        val algo: DistanceBasedAlgorithm<ClusterItem> =
            DistanceBasedAlgorithm()
        for (i in 0..99) {
            algo.addItem(
                JsonTestUtil.TestClusterItem("", LatLng(0.0, 0.0), i.toString(), "")
            )
        }
        assertEquals(100, algo.items.size)
        val items = algo.items
        var counter = 0
        for (item in items) {
            assertEquals(Integer.toString(counter), item.title)
            counter++
        }
    }
}