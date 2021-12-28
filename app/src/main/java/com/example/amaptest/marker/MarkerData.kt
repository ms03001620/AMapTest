package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.repository.data.charging.StationDetail

interface BaseMarkerData {
    fun getStation(): StationDetail?

    fun getSize(): Int

    fun getLatlng(): LatLng?

    fun getId(): String
}

class MarkerCluster(val list: Cluster<ClusterItem>) : BaseMarkerData {
    override fun getStation(): StationDetail? {
        if (list is StaticCluster<ClusterItem>) {
            val items = list.items
            if (items is LinkedHashSet) {
                val firstItem = items.first()
                if (firstItem is StationClusterItem) {
                    return firstItem.stationDetail
                }
            }
        }
        return null
    }

    override fun getSize() = list.size

    override fun getLatlng(): LatLng? {
        return list.position
    }

    override fun getId() = list.position.hashCode().toString()

    override fun hashCode(): Int {
        return getId().hashCode() + getSize()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }
}

class MarkerSingle(val stationDetail: StationDetail, val latLng: LatLng?) : BaseMarkerData {
    override fun getStation() = stationDetail

    override fun getSize() = 1

    override fun getLatlng(): LatLng? {
        return latLng
    }

    override fun getId() = stationDetail.id ?: ""

    override fun hashCode(): Int {
        return getId().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }
}

object MarkerDataFactory {
    fun create(list: Set<Cluster<ClusterItem>>): MutableList<BaseMarkerData> {
        val result = mutableListOf<BaseMarkerData>()

        list.forEach {
            if (it.items?.size == 1) {
                val f = it.items?.toList()!![0] as StationClusterItem
                result.add(MarkerSingle(f.stationDetail, it.position))
            } else {
                result.add(MarkerCluster(it))
            }
        }

        return result
    }
}