package com.example.amaptest.marker

import com.example.amaptest.marker.ClusterUtils.subSize
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm
import org.junit.Assert.*

import org.junit.Test

class ClusterUtilsTest {
    private val stationsList = JsonTestUtil.readStation("json_stations.json")

    @Test
    fun createTrackDataBase() {
        // A, B, C -> ABC
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3)
        )
        val c = JsonTestUtil.mock(stationsList.subList(0, 3))

        ClusterUtils.createTrackData(c[0], p).let {
            assertEquals(3, it.subSize())
            assertTrue(it.subNodeNoMove?.isNoMove == true)
            assertFalse(it.subNodeList[0].isNoMove)
            assertFalse(it.subNodeList[1].isNoMove)
        }
    }

    @Test
    fun createTrackDataBaseRevert() {
        // ABC -> A, B, C
        val p = JsonTestUtil.mock(stationsList.subList(0, 3))
        val c = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3)
        )

        ClusterUtils.createTrackData(c[0], p).let {
            assertEquals(1, it.subSize())
            assertTrue(it.subNodeNoMove?.isNoMove == true)
        }

        ClusterUtils.createTrackData(c[1], p).let {
            assertEquals(1, it.subSize())
            assertFalse(it.subNodeList[0].isNoMove)
        }

        ClusterUtils.createTrackData(c[2], p).let {
            assertEquals(1, it.subSize())
            assertFalse(it.subNodeList[0].isNoMove)
        }
    }

    @Test
    fun createTrackDataBasePiece() {
        // AB 12 -> A1, B2
        val prev = JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))
        val curr = JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(3, 4).first()
            ),
        )

        ClusterUtils.createTrackData(curr[0], prev).let {
            assertEquals(1, it.subNodeList.size)
            assertNotNull(it.subNodeNoMove)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[0].nodeType)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeNoMove?.nodeType)
            assertTrue(it.subNodeNoMove?.isNoMove == true)
            assertFalse(it.subNodeList[0].isNoMove)
        }

        ClusterUtils.createTrackData(curr[1], prev).let {
            assertEquals(2, it.subNodeList.size)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[0].nodeType)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[1].nodeType)
            assertFalse(it.subNodeList[0].isNoMove)
            assertFalse(it.subNodeList[1].isNoMove)
        }
    }

    @Test
    fun createTrackDataBasePieceRevert() {
        // A1, B2 -> AB, 12
        val prev = JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(3, 4).first()
            ),
        )
        val curr = JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))

        ClusterUtils.createTrackData(curr[0], prev).let {
            assertEquals(1, it.subNodeList.size)
            assertNotNull(it.subNodeNoMove)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[0].nodeType)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeNoMove?.nodeType)
            assertTrue(it.subNodeNoMove?.isNoMove == true)
            assertFalse(it.subNodeList[0].isNoMove)
        }

        ClusterUtils.createTrackData(curr[1], prev).let {
            assertEquals(2, it.subNodeList.size)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[0].nodeType)
            assertEquals(ClusterUtils.NodeType.PIECE, it.subNodeList[1].nodeType)
            assertFalse(it.subNodeList[0].isNoMove)
            assertFalse(it.subNodeList[1].isNoMove)
        }
    }

    @Test
    fun createTrackData() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(2, 3)
        )
        val c = JsonTestUtil.mock(stationsList.subList(0, 3))

        assertEquals(1, ClusterUtils.createTrackData(c.first(), p).subNodeList.size)
        assertNotNull(ClusterUtils.createTrackData(c.first(), p).subNodeNoMove)
    }

    @Test
    fun createTrackData1In2() {
        val p = JsonTestUtil.mock(
            stationsList.subList(0, 2),
            stationsList.subList(2, 3)
        )
        val c = JsonTestUtil.mock(stationsList.subList(0, 1))

        assertNotNull(ClusterUtils.createTrackData(c.first(), p).subNodeNoMove)
        assertTrue(ClusterUtils.createTrackData(c.first(), p).isExpTask)
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

        assertEquals(3, node.subSize())
        assertEquals(2, node.subNodeList.count {
            it.nodeType == ClusterUtils.NodeType.PREV_IN_CURR
        })

        assertNotNull(node.subNodeNoMove)
        assertEquals(ClusterUtils.NodeType.PREV_IN_CURR, node.subNodeNoMove?.nodeType)
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

        val a =
            result.count { it.subNodeList.firstOrNull()?.nodeType == ClusterUtils.NodeType.CURR_IN_PREV }
        val b =
            result.count { it.subNodeNoMove?.nodeType == ClusterUtils.NodeType.CURR_IN_PREV }
        assertEquals(
            3,
            a + b
        )
    }

    @Test
    fun processBig() {
        processBigImpl(true)
        processBigImpl(false)
    }

    fun processBigImpl(enableDelSame: Boolean) {
        val algorithm = DistanceBasedAlgorithm<ClusterItem>()
        algorithm.addItems(
            JsonTestUtil.readStation("json_stations.json").map { StationClusterItem(it) })


        val sss = mutableListOf<Int>()

        ClusterUtils.loops(7f, 17f, .4f, callback = { prevIndex: Float, currIndex: Float ->
            val p = MarkerDataFactory.create(algorithm.getClusters(prevIndex))
            val c = MarkerDataFactory.create(algorithm.getClusters(currIndex))
            val subPrev = p.toMutableList()
            val subCurr = c.toMutableList()

            if (enableDelSame) {
                ClusterUtils.delSame(subPrev, subCurr)
            }

            isSame(subPrev, subCurr, sss)
        })

        println("total: ${sss.size}")
    }

    private fun isSame(
        p: MutableList<BaseMarkerData>,
        c: MutableList<BaseMarkerData>,
        sss: MutableList<Int>
    ) {
        val result = c.map { ClusterUtils.createTrackData(it, p) }

        result.forEach {
            val currPos = it.node.getLatlng()

            val count = it.subNodeList.count {
                ClusterUtils.isSamePosition(currPos,  it.subNode.getLatlng())
            }

            if (count == 0) {
                sss.add(1)
            }
        }

        val pCount = p.sumOf { it.getSize() }
        val cCount = c.sumOf { it.getSize() }
        val nCount = result.sumOf { it.node.getSize() }
        val nSubCount = result.sumOf { it.subNodeList.sumOf { it.subNode.getSize() } }
        val nSubNoMoveCount = result.sumOf { it.subNodeNoMove?.subNode?.getSize()?:0 }
        unCheckCase(result)
        //println("-----pCount:$pCount")
        assertEquals(pCount, cCount)
        assertEquals(pCount, nCount)
        assertEquals(pCount, nSubCount + nSubNoMoveCount)
    }

    private fun unCheckCase(result: List<ClusterUtils.NodeTrack>) {
        // subNodeList.size == 1 时不包含PIECE数据，未确定原因
        result.filter {
            it.isExpTask
        }.map {
            it.subNodeList.firstOrNull()?.nodeType
        }.count {
            it?.name == ClusterUtils.NodeType.PIECE.name //     SINGLE, PARTY, PIECE
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

        assertTrue(ClusterUtils.isAllItemInParent(prev, child))
        assertFalse(ClusterUtils.isAllItemInParent(child, prev))
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

        assertTrue(ClusterUtils.isAllItemInParent(prev, child))
        assertFalse(ClusterUtils.isAllItemInParent(child, prev))
    }

    @Test
    fun testIsAllInTargetFalse() {
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 4))
        val childCluster = JsonTestUtil.mock(stationsList.subList(5, 8))

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterUtils.isAllItemInParent(prev, child))
    }

    @Test
    fun testIsAllInTargetNull() {
        val prevCluster = JsonTestUtil.mock(stationsList.subList(0, 4))
        val prev = (prevCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterUtils.isAllItemInParent(null, prev))
        assertFalse(ClusterUtils.isAllItemInParent(prev, null))
    }

    @Test
    fun zoomWithParam() {
        mutableListOf<Float>().apply {
            ClusterUtils.loops(1f, 3f, 1f, callback = { f, s ->
                this.add(f)
                this.add(s)
            })
        }.let {
            assertEquals("[1.0, 2.0, 2.0, 3.0, 3.0, 2.0, 2.0, 1.0]", it.toString())
        }
    }

    @Test
    fun zoomWithParamV1() {
        mutableListOf<Float>().apply {
            ClusterUtils.loops(1f, 3f, .5f, callback = { f, s ->
                this.add(f)
                this.add(s)
            })
        }.let {
            assertEquals("[1.0, 1.5, 1.5, 2.0, 2.0, 2.5, 2.5, 3.0, 3.0, 2.5, 2.5, 2.0, 2.0, 1.5, 1.5, 1.0]", it.toString())
        }
    }

    @Test
    fun zoomWithParamV2() {
        mutableListOf<Float>().apply {
            ClusterUtils.loops(0f, 1f, .2f, callback = { f, s ->
                this.add(f)
                this.add(s)
            })
        }.let {
            assertEquals("[0.0, 0.2, 0.2, 0.4, 0.4, 0.6, 0.6, 0.8, 0.8, 1.0, 1.0, 0.8, 0.8, 0.6, 0.6, 0.4, 0.4, 0.2, 0.2, 0.0]", it.toString())
        }
    }

    @Test
    fun zoomWithParamV21() {
        mutableListOf<Float>().apply {
            ClusterUtils.loops(0f, 1f, .3f, callback = { f, s ->
                this.add(f)
                this.add(s)
            })
        }.let {
            assertEquals("[0.0, 0.3, 0.3, 0.6, 0.6, 0.9, 0.9, 0.6, 0.6, 0.3, 0.3, 0.0]", it.toString())
        }
    }

    @Test
    fun zoomWithParamV22() {
        mutableListOf<Float>().apply {
            ClusterUtils.loops(7f, 8f, 1f, callback = { f, s ->
                this.add(f)
                this.add(s)
            })
        }.let {
            assertEquals("[7.0, 8.0, 8.0, 7.0]", it.toString())
        }
    }


    @Test
    fun zoomWithParamV3() {
        assertThrows(java.lang.UnsupportedOperationException::class.java) {
            ClusterUtils.loops(2f, 1f, .2f, callback = { f, s -> })
        }
    }

    @Test
    fun zoomWithParamV4() {
        assertThrows(java.lang.IllegalArgumentException::class.java) {
            ClusterUtils.loops(1f, 3f, 5f, callback = { f, s -> })
        }
    }


}
