package com.example.amaptest.marker

import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import com.polestar.base.utils.logw
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
        assert(markers != null)
        assert(count == markers.size)
        markers.forEach {
            markersHashMap[it.title] = it
        }
        return markers
    }

    fun clear() {
        logd("clear", TAG)
        markersHashMap.clear()
        map.clear(true)
    }

    fun clearMarker(keepIds: List<String>) {
        markersHashMap.keys.filterNot {
            keepIds.contains(it)
        }.forEach {
            removeMarker(it)
        }
    }

    fun removeMarker(id: String) {
        val marker = markersHashMap[id]
        if (marker == null) {
            loge("removeMarker null id:${id}", "logicException")
            return
        }
        marker.remove()
        if (marker.isRemoved) {
            markersHashMap.remove(id)
        } else {
            loge("removeMarker remove fail:${id}", "logicException")
        }
    }

    fun addMarker(markerOptions: MarkerOptions): Marker? {
        if (markersHashMap.containsKey(markerOptions.title).not()) {
            val marker = map.addMarker(markerOptions)
            assert(marker != null)
            markersHashMap[markerOptions.title] = marker
            return marker
        } else {
            // throw IllegalArgumentException("markerOptions has added ${markerOptions.title}")
            loge("addMarker duplicate:${markerOptions.title}", TAG)
            return null
        }
    }

    fun getMarker(id: String): Marker? {
        //logd("getMarker:${id}", TAG)
        return markersHashMap[id]
    }

    fun updateMarker(marker: Marker, id: String, icon: BitmapDescriptor?) {
        //logd("updateMarker:${id}", TAG)
        val removed = markersHashMap.remove(marker.title)
        if (removed == null) {
            loge("updateMarker removed null; title:${marker.title}, id: $id", "logicException")
        }
        marker.title = id
        marker.setIcon(icon)
        markersHashMap[id] = marker
    }

    fun getAllMarkers(): List<Marker> {
        return markersHashMap.values.toList()
    }

    companion object{
        const val TAG = "BaseMap"
    }
}