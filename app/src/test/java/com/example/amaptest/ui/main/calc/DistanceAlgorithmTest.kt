package com.example.amaptest.ui.main.calc

import com.example.amaptest.ui.main.quadtree.Cluster
import com.example.amaptest.ui.main.quadtree.ClusterItem
import com.example.amaptest.JsonTestUtil
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DistanceAlgorithmTest {
    private val algorithm = DistanceAlgorithm()

    @Before
    fun setup() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations.json")
        algorithm.feed(list)
    }

    @Test
    fun baseTest() {
        assertNotNull(calc(DistanceInfo(distanceMerge = 3000f, enableCluster = true)))
        assertEquals(1, calc(DistanceInfo(distanceMerge = 3000f, enableCluster = true))?.size ?: 0)
        assertEquals(4, calc(DistanceInfo(distanceMerge = 1000f, enableCluster = true))?.size ?: 0)
        assertEquals(8, calc(DistanceInfo(distanceMerge = 500f, enableCluster = true))?.size ?: 0)
        assertEquals(12, calc(DistanceInfo(distanceMerge = 200f, enableCluster = true))?.size ?: 0)
        assertEquals(14, calc(DistanceInfo(distanceMerge = 100f, enableCluster = true))?.size ?: 0)
        assertEquals(14, calc(DistanceInfo(distanceMerge = 5f, enableCluster = true))?.size ?: 0)
        assertEquals(20, calc(DistanceInfo(distanceMerge = 3000f, enableCluster = false))?.size ?: 0)

        algorithm.feed(emptyList())
        assertEquals(0, calc(DistanceInfo(distanceMerge = 3000f, enableCluster = false))?.size ?: 0)
    }

    private fun calc(distanceInfo: DistanceInfo): Set<Cluster<ClusterItem>>? {
        var result: Set<Cluster<ClusterItem>>? = null
        algorithm.calc(distanceInfo, callback = {
            result = it
        })
        return result
    }
}