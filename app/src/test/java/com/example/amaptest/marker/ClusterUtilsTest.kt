package com.example.amaptest.marker

import org.junit.Assert.*

import org.junit.Test

class ClusterUtilsTest {
    private val stationsList = JsonTestUtil.readStation("json_stations.json")

    @Test
    fun createTrackDataBase() {
        val p = JsonTestUtil.mock(stationsList.subList(0, 3))
        val c = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3)
        )
        val t = ClusterUtils.createTrackData(p.first(), c)
        assertEquals(3, t.subNodeList.size)
    }

    @Test
    fun createTrackData() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(2, 3)
        )
        val c = JsonTestUtil.mock(stationsList.subList(0, 3))

        assertEquals(2, ClusterUtils.createTrackData(c.first(), p).subNodeList.size)
    }

    @Test
    fun createTrackData1In2() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(2, 3)
        )
        val c = JsonTestUtil.mock(stationsList.subList(0, 1))

        assertEquals(1, ClusterUtils.createTrackData(c.first(), p).subNodeList.size)
    }


    @Test
    fun findItems() {
        // AB12 ->   a1, b2
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 2))
        val childCluster = JsonTestUtil.mock(
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first(),
                stationsList.subList(3, 4).first(),
            )
        )
        assertTrue(
            ClusterUtils.findItems(
                (prevCluster[0] as MarkerCluster).list.items,
                (childCluster[0] as MarkerCluster).list.items
            )?.size == 2
        )

        assertNull(ClusterUtils.findItems(null, null))
    }

    @Test
    fun findItemsAB12ToA1B2() {
        // AB12 ->   a1, b2
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))

        val childCluster = JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(3, 4).first()
            )
        )

        assertTrue(
            ClusterUtils.findItems(
                (prevCluster[0] as MarkerCluster).list.items,
                (childCluster[0] as MarkerCluster).list.items
            )?.size == 1
        )

        assertTrue(
            ClusterUtils.findItems(
                (prevCluster[1] as MarkerCluster).list.items,
                (childCluster[1] as MarkerCluster).list.items
            )?.size == 1
        )
    }


}
