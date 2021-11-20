package com.example.amaptest.ui.main.calc

import com.polestar.repository.data.charging.StationDetail
import org.junit.Assert.*
import org.junit.Test

class ClusterTest {

    @Test
    fun changeCalc() {
        val c1 = Cluster(RegionItem(StationDetail(stationid = "a")))

        assertTrue(c1.contains(RegionItem(StationDetail(stationid = "a"))))
        assertFalse(c1.contains(RegionItem(StationDetail(stationid = "b"))))
        assertFalse(c1.contains(RegionItem(StationDetail(stationid = ""))))
    }
}