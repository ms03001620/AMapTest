package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.AssetsReadUtils
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.repository.data.charging.StationDetail
import org.json.JSONArray
import java.util.*
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem


object JsonTestUtil {
    private const val REGEX_INPUT_BOUNDARY_BEGINNING = "\\A"

    fun read(fileName: String?): List<ClusterItem> {
        val stream = JsonTestUtil::class.java.classLoader.getResourceAsStream(fileName)
        val json = Scanner(stream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next()
        // testImplementation 'org.json:json:20210307' for JSONArray
        val array = JSONArray(json)

        return mutableListOf<ClusterItem>().also {
            for (i in 0 until array.length()) {
                val node = array.getJSONObject(i)
                it.add(
                    TestClusterItem(
                        "",
                        LatLng(
                            node.optDouble("lat", Double.NaN),
                            node.optDouble("lng", Double.NaN)
                        ),
                        node.optString("title", ""),
                        node.optString("snippet", "")
                    )
                )
            }
        }
    }

    fun readStation(fileName: String?): List<StationDetail> {
        val stream = JsonTestUtil::class.java.classLoader.getResourceAsStream(fileName)
        val json = Scanner(stream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next()
        return AssetsReadUtils.jsonToStations(json)
    }


    fun mock(vararg paramList: List<StationDetail>): MutableList<BaseMarkerData> {
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

    class TestClusterItem(
        override val id: String = "",
        override val position: LatLng,
        override var title: String?,
        override val snippet: String?
    ) : ClusterItem
}