package com.example.amaptest.marker

import com.example.amaptest.JsonTestUtil
import com.example.amaptest.JsonTestUtil.mock
import io.mockk.*
import org.junit.Assert.*

import org.junit.Test

class MarkerActionTest {
    private val stationsList = JsonTestUtil.readStation("json_stations.json")

    @Test
    fun removed(){
        val mapProxy = mockk<MapProxy>()
        every { mapProxy.deleteMarker(any()) } just Runs
        MarkerAction(mapProxy).removed(mock(stationsList.subList(0, 3), stationsList.subList(3, 6)))
        verify(exactly = 2)  { mapProxy.deleteMarker(any()) }
    }

    @Test
    fun cosp() {

    }


}