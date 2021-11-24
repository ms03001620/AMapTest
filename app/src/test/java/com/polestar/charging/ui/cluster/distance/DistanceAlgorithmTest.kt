package com.polestar.charging.ui.cluster.distance

import com.example.amaptest.JsonTestUtil
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DistanceAlgorithmTest {
    private val algorithm = DistanceAlgorithm()


    @Test
    fun baseBigDataTest() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations570.json")
        assertEquals(570, list.size)
        algorithm.feed(list)

        assertEquals(
            570,
            calc(DistanceInfo(distanceMerge = 3000f, enableCluster = false))?.size ?: 0
        )

        assertEquals(
            1,
            calc(DistanceInfo(distanceMerge = 500000f, enableCluster = true))?.size ?: 0
        )
    }

    @Test
    fun baseTest() {

        val list: List<ClusterItem> = JsonTestUtil.read("json_stations.json")
        algorithm.feed(list)
        assertNotNull(calc(DistanceInfo(distanceMerge = 3000f, enableCluster = true)))
        assertEquals(1, calc(DistanceInfo(distanceMerge = 3000f, enableCluster = true))?.size ?: 0)
        assertEquals(4, calc(DistanceInfo(distanceMerge = 1000f, enableCluster = true))?.size ?: 0)
        assertEquals(8, calc(DistanceInfo(distanceMerge = 500f, enableCluster = true))?.size ?: 0)
        assertEquals(12, calc(DistanceInfo(distanceMerge = 200f, enableCluster = true))?.size ?: 0)
        assertEquals(14, calc(DistanceInfo(distanceMerge = 100f, enableCluster = true))?.size ?: 0)
        assertEquals(14, calc(DistanceInfo(distanceMerge = 5f, enableCluster = true))?.size ?: 0)
        assertEquals(
            20,
            calc(DistanceInfo(distanceMerge = 3000f, enableCluster = false))?.size ?: 0
        )

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