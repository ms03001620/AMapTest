package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import kotlin.collections.HashMap

class ClusterAdapter(val action: OnClusterAction?) {

    interface OnClusterAction {
        fun noChange(data: MutableList<BaseMarkerData>)
        fun onClusterCreateAndMoveTo(removed: MutableList<BaseMarkerData>, map: HashMap<LatLng, MutableList<BaseMarkerData>>)
        fun onClusterMoveToAndRemove(map: HashMap<LatLng, MutableList<BaseMarkerData>>, added: MutableList<BaseMarkerData>)
    }

    var prev: MutableList<BaseMarkerData>? = null
    var lastZoom = 0f

    fun queue(set: MutableList<BaseMarkerData>?, zoom: Float) {
        val isZoomIn = zoom> lastZoom
        lastZoom = zoom

        set?.let {
            if (isZoomIn) {
                process(it)
            } else {
                processZoomOut(it)
            }
        }
    }

    private fun processZoomOut(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            val collapsed = createCollapsedTask(it, curr)
            action?.onClusterMoveToAndRemove(collapsed, curr)
        } ?: run {
            action?.noChange(curr)
        }
        prev = curr
    }

    fun process(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            val exp = createExpTask(it, curr)
            val removed = createRemoveTask(it, curr)
            action?.onClusterCreateAndMoveTo(removed, exp)
        } ?: run {
            action?.noChange(curr)
        }
        prev = curr
    }


    fun createRemoveTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): MutableList<BaseMarkerData> {
        val removedList = mutableListOf<BaseMarkerData>()

        prev.filterIsInstance<MarkerCluster>().forEach { targetCluster ->
            var hasIn = false
            curr.filterIsInstance<MarkerCluster>().forEach {
                if (targetCluster.getId() == it.getId()) {
                    hasIn = true
                }
            }
            if (hasIn.not()) {
                removedList.add(targetCluster)
            }
        }
        return removedList
    }

    fun createCollapsedTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): HashMap<LatLng, MutableList<BaseMarkerData>> {
        val collapsedTask = HashMap<LatLng, MutableList<BaseMarkerData>>()

        prev.forEach { currCluster ->
            val latLng = findPrevLatLng(curr, currCluster)
            latLng?.let {
                findOrCreateClusterList(collapsedTask, it).add(currCluster)
            }
        }

        return collapsedTask
    }

    fun createExpTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): HashMap<LatLng, MutableList<BaseMarkerData>> {
        val expTask = HashMap<LatLng, MutableList<BaseMarkerData>>()

        curr.forEach { currCluster ->
            val latLng = findPrevLatLng(prev, currCluster)
            latLng?.let {
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
