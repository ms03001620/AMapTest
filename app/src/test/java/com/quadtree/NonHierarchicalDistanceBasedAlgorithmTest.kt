package com.quadtree

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NonHierarchicalDistanceBasedAlgorithmTest {

    @Test
    fun baseTest() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations.json")

        assertEquals(20, list.size)

        val algorithm = NonHierarchicalDistanceBasedAlgorithm<ClusterItem>()

        assertTrue(algorithm.addItems(list))

        assertEquals(1, algorithm.getClusters(11f).size)
        assertEquals(1, algorithm.getClusters(12f).size)
        assertEquals(4, algorithm.getClusters(13f).size)
        assertEquals(8, algorithm.getClusters(14f).size)
    }
}