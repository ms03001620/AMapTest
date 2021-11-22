package com.example.amaptest.ui.main

import com.quadtree.ClusterItem
import com.quadtree.JsonTestUtil
import org.junit.Assert.*
import org.junit.Test

class DistanceAlgorithmTest {

    @Test
    fun baseTest() {
        val list: List<ClusterItem> = JsonTestUtil.read("json_stations.json")
        assertEquals(20, list.size)

        val algorithm = DistanceAlgorithm()

       // algorithm.setData(list)
    }

}