package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.showMarker
import com.polestar.repository.data.charging.toLatLng
import java.util.*

interface BaseMarkerData {
    fun getCluster(): Cluster<ClusterItem>

    fun getStation(): StationDetail?

    fun getSize(): Int

    fun getLatlng(): LatLng

    fun getId(): String
}

class MarkerCluster(val list: Cluster<ClusterItem>) : BaseMarkerData {
    override fun getCluster() = list

    override fun getStation(): StationDetail? {
        if (list is StaticCluster<ClusterItem>) {
            val items = list.items
            val firstItem = items.first()
            if (firstItem is StationClusterItem) {
                return firstItem.stationDetail
            }
        }
        return null
    }

    override fun getSize() = list.size

    override fun getLatlng(): LatLng {
        return list.position!!
    }

    // id 不能以latlng作为算法 piece 临时对象具有相同坐标 但是是需要区分的
    override fun getId() = hashCode().toString()

    override fun hashCode(): Int {
        return list.items.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is MarkerCluster) {
            if (this.list.size == other.list.size) {
                return hashCode() == other.hashCode()
            }
        }
        return false
    }

    override fun toString(): String {
        return "c:${getSize()}"
    }
}

class MarkerSingle(val stationDetail: StationDetail, val latLng: LatLng) : BaseMarkerData {
    private val newResult : Cluster<ClusterItem>
    private val ids = stationDetail.id ?: ""

    init {
        latLng.let {
            StaticCluster<ClusterItem>(latLng).let {
                it.add(StationClusterItem(stationDetail))
                newResult = it
            }
        }
    }

    override fun getCluster() = newResult

    override fun getStation() = stationDetail

    override fun getSize() = 1

    override fun getLatlng(): LatLng {
        return latLng
    }

    override fun getId() = ids

    override fun hashCode(): Int {
        return ids.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is MarkerSingle) {
            return hashCode() == other.hashCode()
        }
        return false
    }

    override fun toString(): String {
        return "s:${stationDetail.showMarker()}"
    }
}

object MarkerDataFactory {
    fun create(list: Set<Cluster<ClusterItem>>): MutableList<BaseMarkerData> {
        val result = mutableListOf<BaseMarkerData>()

        list.forEach {
            if (it.items?.size == 1) {
                val f = it.items?.toList()!![0] as StationClusterItem
                result.add(MarkerSingle(f.stationDetail, f.stationDetail.toLatLng()))
            } else {
                result.add(MarkerCluster(it))
            }
        }

        return result
    }

    fun create(list: List<ClusterItem>, latLng: LatLng): BaseMarkerData {
        return if (list.size == 1) {
            val station = (list.first() as StationClusterItem).stationDetail
            MarkerSingle(station, latLng)
        } else {
            val static = StaticCluster<ClusterItem>(latLng)
            list.forEach {
                static.add(it)
            }
            MarkerCluster(static)
        }
    }

    fun createMarkerCluster(station: StationDetail): BaseMarkerData {
        val static = StaticCluster<ClusterItem>(station.toLatLng())
        static.add(StationClusterItem(station))
        return MarkerCluster(static)
    }
}