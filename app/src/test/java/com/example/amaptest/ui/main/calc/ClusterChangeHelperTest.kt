package com.example.amaptest.ui.main.calc

import com.polestar.repository.data.charging.StationDetail
import org.junit.Assert.*

import org.junit.Test

class ClusterChangeHelperTest {

    @Test
    fun changeCalc() {
        val old = mutableListOf<Cluster>(
            Cluster(RegionItem(StationDetail(stationid = "a")))
        )

        val new = mutableListOf<Cluster>(
            Cluster(RegionItem(StationDetail(stationid = "a")))
        )

        val result = ClusterChangeHelper.changeCalc(old, new)

        assertEquals(1, result.size)
    }
}