package com.example.amaptest.ui.main

import com.example.amaptest.ui.main.calc.DistanceInfo
import com.quadtree.Cluster
import com.quadtree.ClusterItem
import com.quadtree.JsonTestUtil
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

        assertEquals(20, calc(DistanceInfo(distanceMerge = 3000f, enableCluster = false))?.size ?: 0)
    }

    private fun calc(distanceInfo: DistanceInfo): Set<Cluster<ClusterItem>>? {
        var result: Set<Cluster<ClusterItem>>? = null
        algorithm.calc(distanceInfo, callback = {
            result = it
        })
        return result
    }
}