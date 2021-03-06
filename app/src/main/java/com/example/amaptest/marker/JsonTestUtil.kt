package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.AssetsReadUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.repository.data.charging.StationDetail
import org.json.JSONArray
import java.util.*
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.robolectric.PolicyBean
import java.lang.reflect.Type


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
                it.add(object : ClusterItem {
                    override val position: LatLng
                        get() = LatLng(
                            node.optDouble("lat", Double.NaN),
                            node.optDouble("lng", Double.NaN)
                        )
                    override val title: String?
                        get() = node.optString("title", "")
                    override val snippet: String?
                        get() = node.optString("snippet", "")
                    override val id: String
                        get() = ""
                })
            }
        }
    }

    fun readPolicy(fileName: String?): PolicyBean {
        val stream = JsonTestUtil::class.java.classLoader.getResourceAsStream(fileName)
        val json = Scanner(stream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next()

        val listType: Type = object : TypeToken<PolicyBean>() {}.type
        val data: PolicyBean = Gson().fromJson(json, listType)
        return data
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