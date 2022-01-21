package com.example.amaptest.marker

import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.polestar.base.utils.logd
import java.lang.IllegalArgumentException

class BaseMap(val map: AMap) {
    /**
     * 缓存所有markers，以及生成改点的数据id
     * AMap 未提供获取所有marker功能 mapScreenMarkers只能获取可见范围内markers
     */
    private val markersHashMap = HashMap<String, Marker>()

    fun addMarkers(
        options: ArrayList<MarkerOptions>,
        moveToCenter: Boolean = false
    ): ArrayList<Marker>? {
        val count = options.size
        logd("createMarkers:${count}", TAG)
        if (count == 0) {
            return null
        }
        val markers = map.addMarkers(options, moveToCenter)
        assert(count == markers.size)
        markers.forEach {
            assert(markersHashMap.contains(it.title).not())
            markersHashMap[it.title] = it
        }
        return markers
    }

    fun clear() {
        logd("clear", TAG)
        markersHashMap.clear()
        map.clear(true)
    }

    fun removeMarker(id: String) {
        logd("removeMarker:${id}", TAG)
        markersHashMap.remove(id)!!.remove()
    }

    fun addMarker(markerOptions: MarkerOptions): Marker? {
        logd("createMarker:${markerOptions.title}", TAG)
        if (markersHashMap.containsKey(markerOptions.title).not()) {
            val marker = map.addMarker(markerOptions)
            assert(marker != null)
            markersHashMap.put(markerOptions.title, marker)
            return marker
        } else {
            throw IllegalArgumentException("markerOptions has added ${markerOptions.title}")
        }
    }

    fun getMarker(id: String): Marker? {
        logd("getMarker:${id}", TAG)
        return markersHashMap[id]
    }

    fun updateMarker(marker: Marker, id: String, icon: BitmapDescriptor?) {
        logd("updateMarker:${id}", TAG)
        val curMarker = markersHashMap.remove(marker.title)
        curMarker!!.title = id
        curMarker!!.setIcon(icon)
        markersHashMap.put(id, curMarker)
    }

    fun getAllMarkers(): List<Marker> {
        return markersHashMap.values.toList()
    }

    companion object{
        const val TAG = "BaseMap"
    }
}