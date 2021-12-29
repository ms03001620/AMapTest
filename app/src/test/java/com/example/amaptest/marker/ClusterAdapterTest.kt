package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.JsonTestUtil
import com.example.amaptest.JsonTestUtil.mock
import org.junit.Assert.*

import org.junit.Test

class ClusterAdapterTest {
    private val stationsList = mockJsonData()

    @Test
    fun process() {
        var result: HashMap<LatLng, MutableList<BaseMarkerData>>? = null

        val adapter = ClusterAdapter(object : ClusterAdapter.OnClusterAction {
            override fun noChange(data: MutableList<BaseMarkerData>) {
            }

            override fun expansion(
                removed: MutableList<BaseMarkerData>,
                map: HashMap<LatLng, MutableList<BaseMarkerData>>
            ) {
                result = map
            }

            override fun collapsed(pair: Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>>) {
            }
        })

        // 1聚合点 -> 2小聚合点
        val prevCluster = mock(stationsList)
        val currentCluster = mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 20)
        )

        adapter.setPrevData(prevCluster)
        adapter.processZoomIn(currentCluster)
        assertEquals(1, result?.size ?: 0)
        assertEquals(2, (result?.get(prevCluster[0].getLatlng())?.size ?: 0))
    }

    @Test
    fun createExpTaskOneSubClusterInBigCluster() {
        val prevCluster = mock(stationsList)
        val currCluster = mock(stationsList.subList(0, 1))

        val expTask = ClusterAdapter(null).createExpTask(prevCluster, currCluster)

        assertEquals(1, expTask.size)
        assertTrue(expTask.containsKey(prevCluster[0].getLatlng()))
    }

    @Test
    fun createRemoveTaskNoRemove() {
        val prevCluster = mock(stationsList)
        val currCluster = mock(stationsList.subList(0, 10), stationsList.subList(11, 19))
        val removedTask = ClusterAdapter(null).createRemoveTask(prevCluster, currCluster)
        assertEquals(0, removedTask.size)
    }

    @Test
    fun createRemoveTaskClusterTo2Sigle() {
        val prevCluster = mock(stationsList.subList(0, 2))
        val currCluster = mock(stationsList.subList(0, 1), stationsList.subList(1, 2))
        val removedTask = ClusterAdapter(null).createRemoveTask(prevCluster, currCluster)
        assertEquals(1, removedTask.size)
    }

    @Test
    fun createCollapsedTaskBase() {
        //Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>>
        val prevCluster = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))
        val currCluster = mock(stationsList.subList(0, 3))
        val task = ClusterAdapter().createCollapsedTask(prevCluster, currCluster)

        assertEquals(3, task.first.values.first().size)
        assertEquals(1, task.second.size)
    }

    @Test
    fun createCollapsedTask() {
        //Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>>
        val prevCluster = mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3),
            stationsList.subList(3, 4)
        )
        val currCluster = mock(stationsList.subList(0, 3), stationsList.subList(3, 4))

        val task = ClusterAdapter().createCollapsedTask(prevCluster, currCluster)

        assertEquals(3, task.first.values.first().size)
        assertEquals(2, task.second.size)
    }

    //TODO createCollapsedTask add more case

    @Test
    fun baseMarkerDataCollection() {
        val prevCluster = mock(
            stationsList.subList(0, 1),
            stationsList.subList(3, 4)
        )

        val target = mock(
            stationsList.subList(3, 4)
        )

        assertEquals(2, prevCluster.size)
        prevCluster.remove(target.first())
        assertEquals(1, prevCluster.size)
    }

    @Test
    fun baseMarkerDataCollectionEquals() {
        val source = mock(
            stationsList.subList(0, 2)
        )

        val source1 = mock(
            stationsList.subList(0, 2)
        )

        assertEquals(source1, source)

        val target = mock(
            stationsList.subList(0, 1)
        )

        val target1 = mock(
            stationsList.subList(0, 1)
        )

        assertEquals(target1, target)
        assertNotEquals(target, source)
    }

    @Test
    fun baseMarkerDataCollectionNotEquals() {
        val source = mock(
            stationsList.subList(0, 2)
        )
        val source1 = mock(
            stationsList.subList(0, 3)
        )
        assertNotEquals(source, source1)
    }

    @Test
    fun markClusterCollectionNotEquals() {
        val source = mock(
            stationsList.subList(0, 2)
        )

        assertEquals(
            source, mock(
                listOf(
                    stationsList.subList(0, 1).first(),
                    stationsList.subList(1, 2).first()
                )
            )
        )

        val target = mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            )
        )
        assertNotEquals(
            source, target
        )
    }


    @Test
    fun baseMarkerDataCollectionRemove() {
        val prevCluster = mock(
            stationsList.subList(0, 2),
            stationsList.subList(3, 4)
        )

        val target = mock(
            stationsList.subList(3, 4)
        )


        prevCluster.removeAll(target)
        assertEquals(1, prevCluster.size)
        assertEquals(2, prevCluster.first().getSize())
    }

    @Test
    fun delSame() {
        val s1 = mock(
            stationsList.subList(0, 2),
            stationsList.subList(3, 4)
        )

        val s2 = mock(
            stationsList.subList(3, 4)
        )

        ClusterAdapter().delSame(s1, s2)
        assertEquals(1, s1.size)
        assertEquals(0, s2.size)
    }

    @Test
    fun delSameMulti() {
        val s1 = mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 3),
            stationsList.subList(3, 4)
        )

        val s2 = mock(
            stationsList.subList(1, 3),
            stationsList.subList(0, 1)
        )

        ClusterAdapter().delSame(s1, s2)
        assertEquals(1, s1.size)
        assertEquals(0, s2.size)
    }

    @Test
    fun findPrevLatLngBase() {
        val prevCluster = mock(stationsList.subList(0, 2))
        val currCluster = mock(stationsList.subList(0, 1))
        val latLng = ClusterAdapter(null).findPrevLatLng(prevCluster, currCluster.first() as MarkerSingle)
        assertEquals((prevCluster.first() as MarkerCluster).getLatlng(), latLng)
    }

    @Test
    fun findPrevLatLngBaseSingle() {
        val prevCluster = mock(stationsList.subList(0, 1))
        val currCluster = mock(stationsList.subList(0, 1))
        val latLng = ClusterAdapter(null).findPrevLatLng(prevCluster, currCluster.first())
        assertEquals(prevCluster.first().getLatlng(), latLng)
    }

    @Test
    fun findPrevLatLngNull() {
        assertNull(
            ClusterAdapter(null).findPrevLatLng(
                mock(stationsList.subList(0, 1)),
                mock(stationsList.subList(1, 2)).first()
            )
        )
        assertNull(
            ClusterAdapter(null).findPrevLatLng(
                mock(stationsList.subList(0, 3)),
                mock(stationsList.subList(3, 4)).first()
            )
        )

        assertNull(
            ClusterAdapter(null).findPrevLatLng(
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
    fun createRemoveTaskClusterTo3Single() {
        val prevCluster = mock(stationsList.subList(0, 3))
        val currCluster = mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(2, 3))
        val removedTask = ClusterAdapter(null).createRemoveTask(prevCluster, currCluster)
        assertEquals(1, removedTask.size)
    }

    @Test
    fun createExpTaskOneClusterInBigCluster() {
        val prevCluster = mock(stationsList)
        val currCluster = mock(stationsList.subList(3, 6))

        val expTask = ClusterAdapter(null).createExpTask(prevCluster, currCluster)

        assertEquals(1, expTask.size)
    }

    @Test
    fun createExpTaskMultiSubClusterInBigCluster() {
        val prevCluster = mock(stationsList)
        val currCluster =
            mock(stationsList.subList(0, 1), stationsList.subList(1, 2), stationsList.subList(3, 6))

        val expTask = ClusterAdapter(null).createExpTask(prevCluster, currCluster)

        assertEquals(1, expTask.size)
        val task = expTask[prevCluster[0].getLatlng()]

        assertEquals(3, (task?.size ?: 0))
    }

    @Test
    fun createExpTaskMultiSubClusterInMultiCluster() {
        val prevCluster = mock(stationsList.subList(0, 9), stationsList.subList(10, 19))
        val currCluster = mock(stationsList.subList(0, 8), stationsList.subList(10, 18))

        val expTask = ClusterAdapter(null).createExpTask(prevCluster, currCluster)

        assertEquals(2, expTask.size)

        assertEquals(1, (expTask[prevCluster[0].getLatlng()]?.size ?: 0))
        assertEquals(1, (expTask[prevCluster[1].getLatlng()]?.size ?: 0))
    }

    @Test
    fun createExpTaskMultiSubClusterInMultiClusterExp() {
        val prevCluster = mock(stationsList.subList(0, 9), stationsList.subList(10, 19))
        val currCluster = mock(
            stationsList.subList(0, 8),
            stationsList.subList(8, 9),
            stationsList.subList(10, 12),
            stationsList.subList(12, 14),
            stationsList.subList(14, 15)
        )

        val expTask = ClusterAdapter(null).createExpTask(prevCluster, currCluster)

        assertEquals(2, expTask.size)

        val group1 = (expTask[prevCluster[0].getLatlng()]?.size ?: 0)
        val group2 = (expTask[prevCluster[1].getLatlng()]?.size ?: 0)
        assertEquals(5, group1 + group2)
    }

    @Test
    fun testIsAllInTarget() {
        val prevCluster = mock(stationsList)
        val childCluster = mock(stationsList.subList(0, 4))

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items

        assertTrue(ClusterAdapter(null).isAllInTarget(prev, child))
    }

    @Test
    fun testIsAllInTargetFalse() {
        val prevCluster = mock(stationsList.subList(0, 4))
        val childCluster = mock(stationsList.subList(5, 8))

        val prev = (prevCluster[0] as MarkerCluster).list.items
        val child = (childCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterAdapter(null).isAllInTarget(prev, child))
    }

    @Test
    fun testIsAllInTargetNull() {
        val prevCluster = mock(stationsList.subList(0, 4))
        val prev = (prevCluster[0] as MarkerCluster).list.items
        assertFalse(ClusterAdapter(null).isAllInTarget(null, prev))
        assertFalse(ClusterAdapter(null).isAllInTarget(prev, null))
    }

    @Test
    fun containInPrev() {
        val prevCluster = mock(stationsList)
        val currCluster = mock(stationsList.subList(0, 1))

        assertTrue(ClusterAdapter(null).containInPrev(prevCluster, currCluster[0]))
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

    private fun mockJsonData() = JsonTestUtil.readStation("json_stations.json")
}