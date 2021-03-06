package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.marker.JsonTestUtil.mock
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.quadtree.DistanceBasedAlgorithm
import org.junit.Assert.*

import org.junit.Test

class ClusterAdapterTest {
    private val stationsList = JsonTestUtil.readStation("json_stations.json")
    private val algorithm = DistanceBasedAlgorithm<ClusterItem>()

    @Test
    fun process() {
        algorithm.addItems(stationsList.map { StationClusterItem(it) })
        val adapter = ClusterAdapter(object : ClusterAdapter.OnClusterAction {
            override fun noChange(data: MutableList<BaseMarkerData>) {
            }

            override fun onAnimTask(animTaskData: AnimTaskData) {
                if (animTaskData.cospList.size > 0) {
                    println(animTaskData)
                }
            }
        })

        val zoomStart = 11
        val zoomEnd = 16

        for (i in zoomStart..zoomEnd) {
            adapter.queue(MarkerDataFactory.create(algorithm.getClusters(i * 1.0f)))
        }
        for (i in zoomEnd - 1 downTo zoomStart) {
            adapter.queue(MarkerDataFactory.create(algorithm.getClusters(i * 1.0f)))
        }
    }

    @Test
    fun processBig() {
        algorithm.addItems(JsonTestUtil.readStation("json_stations570.json").map { StationClusterItem(it) })
        val adapter = ClusterAdapter(object : ClusterAdapter.OnClusterAction {
            override fun noChange(data: MutableList<BaseMarkerData>) {
            }

            override fun onAnimTask(animTaskData: AnimTaskData) {
            }
        })

        val zoomStart = 7
        val zoomEnd = 16

        for (i in zoomStart..zoomEnd) {
            adapter.queue(MarkerDataFactory.create(algorithm.getClusters(i * 1.0f)))
        }
        for (i in zoomEnd - 1 downTo zoomStart) {
            adapter.queue(MarkerDataFactory.create(algorithm.getClusters(i * 1.0f)))
        }
    }

    @Test
    fun animTaskData1To3Node() {
        // p(0,3) c(0,1  1,2  2,3)
        val p = mock(stationsList.subList(0, 3))
        val c = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(0, task.addList.size)
        assertEquals(1, task.deleteList.size)
        assertEquals(0, task.cospList.size)
        assertEquals(1, task.expList.size)
        assertEquals(3, task.expList.values.first().size)

        assertEquals(p.first().getLatlng(), task.expList.keys.first())
    }

    @Test
    fun animTaskData1To3NodeRevers() {
        // p(0,1  1,2  2,3) c (0,3)
        val p = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))
        val c = mock(stationsList.subList(0, 3))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(1, task.addList.size)
        assertEquals(0, task.deleteList.size)
        assertEquals(1, task.cospList.size)
        assertEquals(0, task.expList.size)
        assertEquals(3, task.cospList.values.first().size)

        assertEquals(c.first().getLatlng(), task.cospList.keys.first())
    }

    @Test
    fun animTaskData1To2Node() {
        // p(0,3) c(0,2  2,3)
        val p = mock(stationsList.subList(0, 3))
        val c = mock(stationsList.subList(0, 2), stationsList.subList(2, 3))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(0, task.addList.size)
        assertEquals(1, task.deleteList.size)
        assertEquals(0, task.cospList.size)
        assertEquals(1, task.expList.size)
        assertEquals(2, task.expList.values.first().size)
        assertEquals(p.first().getLatlng(), task.expList.keys.first())
    }

    @Test
    fun animTaskData1To2NodeRevers() {
        // p(0,2  2,3)  c(0,3)
        val p = mock(stationsList.subList(0, 2), stationsList.subList(2, 3))
        val c = mock(stationsList.subList(0, 3))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(1, task.addList.size)
        assertEquals(0, task.deleteList.size)
        assertEquals(1, task.cospList.size)
        assertEquals(0, task.expList.size)
        assertEquals(2, task.cospList.values.first().size)
        assertEquals(c.first().getLatlng(), task.cospList.keys.first())
    }

    @Test
    fun animTaskData1To3Node11111111() {
        val p = mock(stationsList.subList(0, 2), stationsList.subList(2, 4))

        val c = mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(1, 2).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(3, 4).first()
            )
        )

        val task = ClusterAdapter().createAnimTaskData(p, c)

        print(task)

        //assertNotNull(l)
    }


    @Test
    fun animTaskData1To2MarkerCluster() {
        // p(0,4) c(0,2  2,4)
        val p = mock(stationsList.subList(0, 4))
        val c = mock(stationsList.subList(0, 2), stationsList.subList(2, 4))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(0, task.addList.size)
        assertEquals(1, task.deleteList.size)
        assertEquals(0, task.cospList.size)
        assertEquals(1, task.expList.size)
        assertEquals(2, task.expList.values.first().size)
        assertEquals(p.first().getLatlng(), task.expList.keys.first())
    }

    @Test
    fun animTaskData2ClusterTo1MarkerCluster() {
        // p(0,2  2,4) c(0,4)
        val p = mock(stationsList.subList(0, 2), stationsList.subList(2, 4))
        val c = mock(stationsList.subList(0, 4))

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(1, task.addList.size)
        assertEquals(0, task.deleteList.size)
        assertEquals(1, task.cospList.size)
        assertEquals(0, task.expList.size)
        assertEquals(2, task.cospList.values.first().size)
        assertEquals(p.first().getLatlng(), task.cospList.keys.first())
    }

    @Test
    fun animTaskDataFullTask() {
        // p(0,2  2,4  5,8)
        // c(0,4  5,6  6,7  7,8)
        val p = mock(
            stationsList.subList(0, 2),//A
            stationsList.subList(2, 4),//A
            stationsList.subList(5, 8) //B
        )
        val c = mock(
            stationsList.subList(0, 4),//A
            stationsList.subList(5, 6),//B
            stationsList.subList(6, 7),//B
            stationsList.subList(7, 8),//B
        )

        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(1, task.addList.size)
        assertEquals(1, task.deleteList.size)
        assertEquals(1, task.cospList.size)
        assertEquals(1, task.expList.size)
        assertEquals(2, task.cospList.values.first().size)
        assertEquals(3, task.expList.values.first().size)
    }

    @Test
    fun animTaskData3To1() {
        // p(0,1  1,2  2,3)  c(0,3)
        val p = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))
        val c = mock(stationsList.subList(0, 3))


        val task = ClusterAdapter().createAnimTaskData(p, c)
        assertEquals(1, task.addList.size)
        assertEquals(0, task.deleteList.size)
        assertEquals(1, task.cospList.size)
        assertEquals(3, task.cospList.values.first().size)
        assertEquals(0, task.expList.size)
    }

    @Test
    fun markSingleInMarkCluster() {
        val list = mock(stationsList.subList(0, 5))

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(0, 1)).first()
            )
        )

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(1, 2)).first()
            )
        )

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(3, 4)).first()
            )
        )
    }

    @Test
    fun markClusterInMarkCluster() {
        val list = mock(stationsList.subList(0, 5))

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(0, 2)).first()
            )
        )

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(2, 5)).first()
            )
        )

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(2, 4)).first()
            )
        )

        assertEquals(
            list.first().getLatlng(), ClusterAdapter().findLatLng(
                list,
                mock(stationsList.subList(0, 5)).first()
            )
        )
    }

    @Test
    fun targetSameInList() {
        val prevCluster = mock(stationsList.subList(0, 1))
        val currCluster = mock(stationsList.subList(0, 1))
        assertTrue(ClusterAdapter().isSameData(prevCluster, currCluster))

        val latLng = ClusterAdapter().findLatLng(prevCluster, currCluster.first())
        assertEquals(prevCluster.first().getLatlng(), latLng)
    }

    @Test
    fun findLatLngNull() {
        assertNull(
            ClusterAdapter().findLatLng(
                mock(stationsList.subList(0, 1)),
                mock(stationsList.subList(1, 2)).first()
            )
        )
        assertNull(
            ClusterAdapter().findLatLng(
                mock(stationsList.subList(0, 3)),
                mock(stationsList.subList(3, 4)).first()
            )
        )

        assertNull(
            ClusterAdapter().findLatLng(
                mock(stationsList.subList(0, 3)),
                mock(stationsList.subList(3, 5)).first()
            )
        )
    }

    @Test
    fun isSameData() {
        assertFalse(
            ClusterAdapter().isSameData(
                mock(stationsList.subList(0, 1)),
                mock(stationsList.subList(0, 2))
            )
        )

        assertFalse(
            ClusterAdapter().isSameData(
                mock(
                    stationsList.subList(0, 1),
                    stationsList.subList(1, 2),
                    stationsList.subList(2, 3)
                ), mock(stationsList.subList(0, 3))
            )
        )

        assertTrue(
            ClusterAdapter().isSameData(
                mock(stationsList.subList(0, 3)),
                mock(stationsList.subList(0, 3))
            )
        )

        assertTrue(
            ClusterAdapter().isSameData(
                mock(stationsList.subList(0, 1)),
                mock(stationsList.subList(0, 1))
            )
        )

        val prevCluster = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))
        val currCluster = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))

        assertTrue(ClusterAdapter().isSameData(prevCluster, currCluster))

        assertFalse(
            ClusterAdapter().isSameData(
                mock(
                    stationsList.subList(0, 2)
                ),
                mock(
                    listOf(
                        stationsList.subList(0, 1).first(),
                        stationsList.subList(2, 3).first()
                    )
                )
            )
        )
    }

    @Test
    fun containInPrevAll() {
        val map = HashMap<LatLng, String>()
        map.put(LatLng(1.0, 1.0), "String")
        assertTrue(map.containsKey(LatLng(1.0, 1.0)))
    }

    @Test
    fun testDataConvert() {
        assertTrue(mock(stationsList.subList(0, 1))[0] is MarkerSingle)
        assertTrue(mock(stationsList.subList(0, 2))[0] is MarkerCluster)
        assertTrue(mock(stationsList, stationsList).size == 2)
    }
}