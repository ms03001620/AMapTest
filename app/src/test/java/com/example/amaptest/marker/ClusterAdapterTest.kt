package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.JsonTestUtil
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.repository.data.charging.StationDetail
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

            override fun onClusterCreateAndMoveTo(map: HashMap<LatLng, MutableList<BaseMarkerData>>) {
                result = map
            }
        })

        // 1聚合点 -> 2小聚合点
        val prevCluster = mock(stationsList)
        val currentCluster = mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 20)
        )

        adapter.process(prevCluster)
        adapter.process(currentCluster)
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

    private fun mock(vararg paramList: List<StationDetail>): MutableList<BaseMarkerData> {
        hashSetOf<Cluster<ClusterItem>>().also { hash ->
            paramList.forEach { list ->
                var staticCluster: StaticCluster<ClusterItem>? = null
                list.map { stationDetail ->
                    StationClusterItem(stationDetail)
                }.forEach { stationClusterItem ->
                    staticCluster?.add(stationClusterItem)
                        ?: StaticCluster<ClusterItem>(stationClusterItem.position).let {
                            it.add(stationClusterItem)
                            hash.add(it)
                            staticCluster = it
                        }
                }
            }
        }.let {
            return MarkerDataFactory.create(it)
        }
    }

    private fun mockJsonData() = JsonTestUtil.readStation("json_stations.json")
}