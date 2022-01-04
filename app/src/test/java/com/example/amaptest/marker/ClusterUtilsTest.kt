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
    fun nodeTrackCase4To1() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 1),//A
            stationsList.subList(1, 2),//B
            stationsList.subList(2, 4),//CD
        )

        val c = JsonTestUtil.mock(
            stationsList.subList(0, 4)//ABCD
        )

        val node = ClusterUtils.createTrackData(c.first(), p)

        assertEquals(3, node.subNodeList.size)
        assertEquals(2, node.subNodeList.count {
            it.nodeType == ClusterUtils.NodeType.SINGLE
        })
        assertEquals(1, node.subNodeList.count {
            it.nodeType == ClusterUtils.NodeType.PARTY
        })
    }

    @Test
    fun nodeTrackCase4To1Revert() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 4)//ABCD
        )
        val c = JsonTestUtil.mock(
            stationsList.subList(0, 1),//A
            stationsList.subList(1, 2),//B
            stationsList.subList(2, 4),//CD
        )
        //TODO 分析createTrackData生成数据和其类型的关系，以便组织响应的动画

        val result = c.map { ClusterUtils.createTrackData(it, p) }
        assertEquals(3, result.size)
        assertEquals(
            3,
            result.count { it.subNodeList.first().nodeType == ClusterUtils.NodeType.PARTY })
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

    @Test
    fun testIsAllInTarget() {
        val prevCluster = JsonTestUtil.mock(stationsList)
        val childCluster = JsonTestUtil.mock(stationsList.subList(0, 4))

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items

        assertTrue(ClusterUtils.isClusterContainerItems(prev, child))
        assertFalse(ClusterUtils.isClusterContainerItems(child, prev))
    }

    @Test
    fun testIsAllInTargetNoOrder() {
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 4))

        val childCluster = JsonTestUtil.mock(
            listOf(
                stationsList[2],
                stationsList[0],
            )
        )

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items

        assertTrue(ClusterUtils.isClusterContainerItems(prev, child))
        assertFalse(ClusterUtils.isClusterContainerItems(child, prev))
    }

    @Test
    fun testIsAllInTargetFalse() {
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 4))
        val childCluster = JsonTestUtil.mock(stationsList.subList(5, 8))

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterUtils.isClusterContainerItems(prev, child))
    }

    @Test
    fun testIsAllInTargetNull() {
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 4))
        val prev = (prevCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterUtils.isClusterContainerItems(null, prev))
        assertFalse(ClusterUtils.isClusterContainerItems(prev, null))
    }


}
