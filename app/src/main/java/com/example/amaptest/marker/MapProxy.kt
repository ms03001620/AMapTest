package com.example.amaptest.marker

import android.content.Context
import android.graphics.Bitmap
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.polestar.base.utils.logd
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.showMarker
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

class MapProxy(private val map: AMap, private val context: Context) {
    private val iconGenerator = IconGenerator(context)
    private val set = ConcurrentHashMap<String, Marker>()

    fun createMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        baseMarkerDataList.forEach {
            createMarker(it, it.getLatlng())
        }
    }

    fun createMarker(baseMarkerData: BaseMarkerData){
        createMarker(baseMarkerData, baseMarkerData.getLatlng())
    }

    fun createMarker(baseMarkerData: BaseMarkerData, latLng: LatLng?): Marker {
        baseMarkerData.getId().let { id ->
            val oldMarker = set[id]
            if (oldMarker == null) {
                //logd("222222: create 1", "______")
                val option = createOptionsToPosition(baseMarkerData, latLng)
                val marker = createMarker(option)
                if (marker != null) {
                    set[id] = marker
                } else {
                    throw IllegalStateException("create marker failed")
                }
                return marker
            } else {
                //logd("222222: create 2", "______")
                // throw IllegalStateException("set.containsKey(${id})")
                // logd("updateMarker $id")
                updateMarker(oldMarker, baseMarkerData, latLng)
                return oldMarker
            }
        }
    }

    fun createOptionsToPosition(baseMarkerData: BaseMarkerData, latLng: LatLng?): MarkerOptions {
        val options = when (baseMarkerData) {
            is MarkerCluster -> {
                stationToClusterOptions(baseMarkerData.getSize(), latLng)
            }
            is MarkerSingle -> {
                stationToMarkerOptions(baseMarkerData.stationDetail, latLng)
            }
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }
        return options
    }

    fun updateMarker(marker: Marker, baseMarkerData: BaseMarkerData) {
        val start = System.currentTimeMillis()
        val t = createBitmapDescriptor(baseMarkerData)
        logd("ssss ${System.currentTimeMillis()-start}", "_____")
        assert(t != null)
        marker.setIcon(t)
    }

    fun updateMarker(marker: Marker, baseMarkerData: BaseMarkerData, forceLatLng: LatLng? = null) {
        //TODO 相同点相同数据的优化问题
        val finalLatLng = forceLatLng ?: marker.position
        //logd("11111111c"+baseMarkerData+", class:${baseMarkerData.javaClass.simpleName}, lng:$finalLatLng", "______")
        marker.setMarkerOptions(createOptionsToPosition(baseMarkerData, finalLatLng))
        //logd("c", "_____")
    }

    fun removeMarkers(remove: MutableList<BaseMarkerData>) {
        remove.forEach {
            set.remove(it.getId())?.remove()
        }
    }

    fun removeMarker(id: String?) {
        set.remove(id)?.remove()
    }

    private fun createMarker(markerOptions: MarkerOptions): Marker? {
        markerOptions.isFlat = true
        return map.addMarker(markerOptions)
    }

    fun getCollapsedBitmapDescriptor2(total: String): Bitmap {
        val p = iconGenerator.makeIconCluster(total)
        return p
    }

    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(total))
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        return BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIconCluster(clusterSize.toString()))
    }

    private fun createBitmapDescriptor(baseMarkerData: BaseMarkerData): BitmapDescriptor? {
        return when (baseMarkerData) {
            is MarkerCluster -> getClusterBitmapDescriptor(baseMarkerData.getSize())
            is MarkerSingle -> getCollapsedBitmapDescriptor(baseMarkerData.stationDetail.showMarker())
            else -> throw UnsupportedOperationException("type:$baseMarkerData")
        }
    }

    private fun stationToClusterOptions(size: Int, latLng: LatLng?) =
        MarkerOptions()
            .position(latLng)
            .icon(getClusterBitmapDescriptor(size))
            .infoWindowEnable(false)

    private fun stationToMarkerOptions(station: StationDetail, latLng: LatLng? = null) =
        MarkerOptions()
            .position(latLng ?: LatLng(station.lat ?: Double.NaN, station.lng ?: Double.NaN))
            .icon(getCollapsedBitmapDescriptor(station.showMarker()))
            .infoWindowEnable(false)

    fun getMarker(latLng: LatLng): Marker? {
        for ((key, value) in set) {
            if (ClusterUtils.isSamePosition(value.position, latLng)) {
                return value
            }
        }
        return null
    }

    fun getMarker(baseMarkerData: BaseMarkerData): Marker? {
        return set.getOrDefault(baseMarkerData.getId(), null)
    }

    fun clear() {
        set.clear()
        map.clear(true)
    }

    fun removeAllMarker(removeList: List<LatLng>) {
        set.filter { map ->
            removeList.firstOrNull{ClusterUtils.isSamePosition(it, map.value.position)} !=null
        }.forEach {
            set.remove(it.key)?.remove()
        }
    }
}