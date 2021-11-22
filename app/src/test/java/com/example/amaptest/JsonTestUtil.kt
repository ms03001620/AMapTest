package com.example.amaptest

import com.amap.api.maps.model.LatLng
import com.example.amaptest.ui.main.quadtree.ClusterItem
import org.json.JSONArray
import java.util.*

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

    class TestClusterItem(
        override val position: LatLng,
        override var title: String?,
        override val snippet: String?
    ) : ClusterItem
}