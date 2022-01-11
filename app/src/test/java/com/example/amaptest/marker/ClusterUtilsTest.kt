package com.example.amaptest.marker

import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm
import org.junit.Assert.*

import org.junit.Test
import java.util.*

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
    fun createTrackDataBase1() {
        val p = JsonTestUtil.mock(stationsList.subList(0, 3))
        val c = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3)
        )

        val b1 = ClusterUtils.process(p, c)
        val b2 = ClusterUtils.process(c, p)


        assertNotNull(b1)
    }

    @Test
    fun createTrackDataBase2() {
        val p = JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))
        val c = JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(3, 4).first()
            ),
        )

        val b1 = ClusterUtils.process(p, c)
        val b2 = ClusterUtils.process(c, p)

        assertNotNull(b1)
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
        val result = c.map { ClusterUtils.createTrackData(it, p) }
        assertEquals(3, result.size)
        assertEquals(
            2,
            result.count { it.subNodeList.first().nodeType == ClusterUtils.NodeType.SINGLE })
    }

    @Test
    fun processBig() {
        processBigImpl(true)
        processBigImpl(false)
    }

    fun processBigImpl(enableDelSame: Boolean) {
        val algorithm = DistanceBasedAlgorithm<ClusterItem>()
        algorithm.addItems(
            JsonTestUtil.readStation("json_stations570.json").map { StationClusterItem(it) })

        val zoomStart = 7
        val zoomEnd = 16

        for (i in zoomStart..zoomEnd step 2) {
            val index1 = i * 1.0f
            val index2 = (i + 1) * 1.0f

            //println(index1)
            //println(index2)

            val p = MarkerDataFactory.create(algorithm.getClusters(index1))
            val c = MarkerDataFactory.create(algorithm.getClusters(index2))
            val subPrev = p.toMutableList()
            val subCurr = c.toMutableList()

            if (enableDelSame) {
                ClusterUtils.delSame(subPrev, subCurr)
            }

            isSame(subPrev, subCurr)
        }
        println("-----")
        for (i in zoomEnd downTo zoomStart step 2) {
            val index1 = i * 1.0f
            val index2 = (i - 1) * 1.0f
            //println(index1)
            //println(index2)

            val p = MarkerDataFactory.create(algorithm.getClusters(index1))
            val c = MarkerDataFactory.create(algorithm.getClusters(index2))
            val subPrev = p.toMutableList()
            val subCurr = c.toMutableList()

            if (enableDelSame) {
                ClusterUtils.delSame(subPrev, subCurr)
            }
            isSame(subPrev, subCurr)
        }
    }

    private fun isSame(p: MutableList<BaseMarkerData>, c: MutableList<BaseMarkerData>) {
        val result = c.map { ClusterUtils.createTrackData(it, p) }

        result.filter {
            it.subNodeList.size > 1
        }.filter {
            it.subNodeList.firstNotNullOf {
                it.nodeType == ClusterUtils.NodeType.PIECE
            }
        }.forEach {
            val string = it.subNodeList.map {
                "${it.nodeType}"
            }

            println("pp sub size:${it.subNodeList.size}, types:${string}")
        }

        val pCount = p.sumOf { it.getSize() }
        val cCount = c.sumOf { it.getSize() }
        val nCount = result.sumOf { it.node.getSize() }
        val nSubCount = result.sumOf { it.subNodeList.sumOf { it.subNode.getSize() } }
        unCheckCase(result)
        //println("-----pCount:$pCount")
        assertEquals(pCount, cCount)
        assertEquals(pCount, nCount)
        assertEquals(pCount, nSubCount)
    }

    private fun unCheckCase(result: List<ClusterUtils.NodeTrack>) {
        // subNodeList.size == 1 时不包含PIECE数据，未确定原因
        result.filter {
            it.subNodeList.size == 1
        }.map {
            it.subNodeList.first().nodeType
        }.count {
            it.name == "PIECE" //     SINGLE, PARTY, PIECE
        }.let {
            assertEquals(0, it)
        }
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
    fun delSameMarkSingle() {
        val s1 = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(3, 4)
        )

        val s2 = JsonTestUtil.mock(
            stationsList.subList(3, 4)
        )

        ClusterUtils.delSame(s1, s2)

        assertTrue(s1.first() is MarkerCluster)
        assertEquals(1, s1.size)
        assertEquals(0, s2.size)
    }

    @Test
    fun delSameMarkCluster() {
        val s1 = JsonTestUtil.mock(
            stationsList.subList(0, 5),
        )

        val s2 = JsonTestUtil.mock(
            stationsList.subList(0, 5),
            stationsList.subList(1, 3),
        )

        ClusterUtils.delSame(s1, s2)
        assertEquals(0, s1.size)
        assertEquals(1, s2.size)
    }

    @Test
    fun delSameMarkCluste1r() {
        val s1 = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 3),
            stationsList.subList(3, 4)
        )

        val s2 = JsonTestUtil.mock(
            stationsList.subList(1, 3),
            stationsList.subList(0, 1)
        )

        ClusterUtils.delSame(s1, s2)
        assertEquals(1, s1.size)
        assertEquals(0, s2.size)
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
