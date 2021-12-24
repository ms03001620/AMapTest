package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.logd
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import kotlin.collections.HashMap

class ClusterAdapter(val action: OnClusterAction?) {

    interface OnClusterAction {
        fun noChange(data: MutableList<BaseMarkerData>)
        fun onClusterCreateAndMoveTo(map: HashMap<LatLng, MutableList<BaseMarkerData>>)
    }

    var prev: MutableList<BaseMarkerData>? = null

    fun queue(set: MutableList<BaseMarkerData>?) {
        process(set!!)
    }

    fun process(curr: MutableList<BaseMarkerData>) {
        if (prev != null) {
            action?.onClusterCreateAndMoveTo(createExpTask(prev!!, curr))
        } else {
            prev = curr
            action?.noChange(curr)
        }
    }

    fun createExpTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): HashMap<LatLng, MutableList<BaseMarkerData>> {
        val expTask = HashMap<LatLng, MutableList<BaseMarkerData>>()

        curr.forEach { currCluster ->
            val latlng = findPrevLatLng(prev, currCluster)
            latlng?.let {
                findOrCreateClusterList(expTask, it).add(currCluster)
            }
        }

        return expTask
    }

    fun findOrCreateClusterList(
        expTask: HashMap<LatLng, MutableList<BaseMarkerData>>,
        key: LatLng
    ): MutableList<BaseMarkerData> {
        var result = expTask[key]
        if (result == null) {
            result = mutableListOf<BaseMarkerData>()
            expTask[key] = result
        }
        return result
    }

    fun containInPrev(prev: MutableList<BaseMarkerData>, element: BaseMarkerData): Boolean {
        return findPrevLatLng(prev, element) != null
    }

    fun findPrevLatLng(prev: MutableList<BaseMarkerData>, target: BaseMarkerData): LatLng? {
        prev.forEach { element ->
            val fromLatLng = element.getLatlng()
            when (element) {
                is MarkerCluster -> {
                    when (target) {
                        is MarkerSingle -> {
                            if (isAllInBaseMarker(element.list.items, target)) {
                                return fromLatLng
                            }
                        }
                        is MarkerCluster -> {
                            if (isAllInTarget(element.list.items, target.list.items)) {
                                return fromLatLng
                            }
                        }
                    }
                }
            }

        }
        return null
    }

    fun isAllInBaseMarker(
        parent: MutableCollection<ClusterItem>?,
        child: BaseMarkerData?
    ): Boolean {
        if (parent == null || child == null) {
            return false
        }

        if (child is MarkerSingle) {
            child.stationDetail.id?.let { targetId ->
                parent.filter {
                    (it as StationClusterItem).stationDetail.id != null
                }.map {
                    (it as StationClusterItem).stationDetail.id!!
                }.contains(targetId).let {
                    return it
                }
            }

        }
        return false
    }

    fun isAllInTarget(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ): Boolean {
        if (parent == null || child == null) {
            return false
        }

        val childIds = child.filter {
            (it as StationClusterItem).stationDetail.id != null
        }.map {
            (it as StationClusterItem).stationDetail.id!!
        }

        parent.filter {
            (it as StationClusterItem).stationDetail.id != null
        }.map {
            (it as StationClusterItem).stationDetail.id!!
        }.containsAll(childIds).let {
            return it
        }
    }

}
