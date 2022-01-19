package com.example.amaptest.marker

import com.example.amaptest.marker.JsonTestUtil.mock
import io.mockk.*

import org.junit.Test

class MarkerActionTest {
    private val stationsList = JsonTestUtil.readStation("json_stations.json")

    @Test
    fun removed(){
/*        val mapProxy = mockk<MapProxy>()
        every { mapProxy.removeMarker(any()) } just Runs
        every { mapProxy.removeMarkers(any()) } just Runs
        MarkerAction(mapProxy).remove(mock(stationsList.subList(0, 3), stationsList.subList(3, 6)))
        verify(exactly = 1)  { mapProxy.removeMarkers(any()) }*/
    }

    @Test
    fun cosp() {

    }


}