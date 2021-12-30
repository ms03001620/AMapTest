package com.example.amaptest.marker

import androidx.annotation.VisibleForTesting
import com.amap.api.maps.model.LatLng
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StationClusterItem
import kotlin.collections.HashMap

class ClusterAdapter(val action: OnClusterAction? = null) {

    interface OnClusterAction {
        fun noChange(data: MutableList<BaseMarkerData>)

        /**
         * 展开，removed原节点消失， map latLng原节点地址 list各自终点
         * 原来节点先消失，然后从改节点分裂出子节点，并通过动画移动到各自终点（终点如果已存在则更新最终节点）
         */
        fun expansion(removed: MutableList<BaseMarkerData>, map: HashMap<LatLng, MutableList<BaseMarkerData>>)

        /**
         * 合拢，added合拢后形成的新节点， map LatLng合拢节点的终点， list各自的起点
         * 子节点从各自节点通过动画移动到合拢节点，消失，然后创建合拢节点
         */
        fun collapsed(pair: Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>>)
    }

    var prev: MutableList<BaseMarkerData>? = null
    var lastZoom = 0f


    fun process1(set: MutableList<BaseMarkerData>?, zoom: Float) {
        val isZoomIn = zoom> lastZoom
        lastZoom = zoom

        set?.let {
            if (prev == null || isSameData(prev, set)) {
                prev?.let {
                    logd("same:${isSameData(it, set)}", "queue")
                }
                action?.noChange(it)
            } else {
                if (isZoomIn) {
                    processZoomIn(it)
                } else {
                    processZoomOut(it)
                }
            }
            prev = it
        }
    }

    fun process(curr: MutableList<BaseMarkerData>?, zoom: Float) {
        if (curr == null) {
            throw IllegalAccessException("curr null")
        }
        if (prev == null || isSameData(prev, curr)) {
            logd("same", "process noChange")
            action?.noChange(curr)
        } else {
            val subPrev = prev!!.toMutableList()
            val subCurr = curr.toMutableList()

            delSame(subPrev, subCurr)
            assert(getMarkerListSize(subPrev) == getMarkerListSize(subCurr))

        }
        prev = curr
    }

    fun getMarkerListSize(list: MutableList<BaseMarkerData>?): Int {
        var total = 0
        list?.forEach {
            total += it.getSize()
        }
        return total
    }

    fun queue(set: MutableList<BaseMarkerData>?, zoom: Float) {
        val start = System.currentTimeMillis()
        process(set, zoom)
        logd("queue spend:${System.currentTimeMillis() - start}")
    }

    private fun processZoomOut(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            action?.collapsed(createCollapsedTask(it, curr))
        }
    }

    fun processZoomIn(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            val exp = createExpTask(it, curr)
            val removed = createRemoveTask(it, curr)
            action?.expansion(removed, exp)
        }
    }

    fun isSameData(
        prev: MutableList<BaseMarkerData>?,
        curr: MutableList<BaseMarkerData>
    ) = curr == prev

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
        prev1: MutableList<BaseMarkerData>,
        curr1: MutableList<BaseMarkerData>
    ): Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>> {
        val collapsedTask = HashMap<LatLng, MutableList<BaseMarkerData>>()

        val prev = prev1.toMutableList()
        val curr = curr1.toMutableList()

        prev.forEach { currCluster ->
            val latLng = findLatLng(curr, currCluster)
            latLng?.let {
                findOrCreateClusterList(collapsedTask, it).add(currCluster)
            }
        }

        return Pair(collapsedTask, curr)
    }

    fun delSame(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ){
        val prevCopy = prev.toMutableList()
        prev.removeAll(curr)
        curr.removeAll(prevCopy)
    }

    fun createExpTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): HashMap<LatLng, MutableList<BaseMarkerData>> {
        val expTask = HashMap<LatLng, MutableList<BaseMarkerData>>()

        curr.forEach { currCluster ->
            val latLng = findLatLng(prev, currCluster)
            latLng?.let {
                findOrCreateClusterList(expTask, it).add(currCluster)
            }
        }

        return expTask
    }

    fun findOrCreateClusterList(
        collapsedTask: HashMap<LatLng, MutableList<BaseMarkerData>>,
        key: LatLng
    ): MutableList<BaseMarkerData> {
        var result = collapsedTask[key]
        if (result == null) {
            result = mutableListOf<BaseMarkerData>()
            collapsedTask[key] = result
        }
        return result
    }

    fun containInPrev(prev: MutableList<BaseMarkerData>, element: BaseMarkerData): Boolean {
        return findLatLng(prev, element) != null
    }

    fun findLatLng(markDataList: MutableList<BaseMarkerData>, target: BaseMarkerData): LatLng? {
        markDataList.forEach { baseMarkerData ->
            val fromLatLng = baseMarkerData.getLatlng()
            when (baseMarkerData) {
                is MarkerCluster -> {
                    when (target) {
                        is MarkerSingle -> {
                            if (isAllInBaseMarker(baseMarkerData.list.items, target)) {
                                return fromLatLng
                            }
                        }
                        is MarkerCluster -> {
                            if (isAllInTarget(baseMarkerData.list.items, target.list.items)) {
                                return fromLatLng
                            }
                        }
                    }
                }
                is MarkerSingle -> {
                    if (baseMarkerData.getId() == target.getId()) {
                        return fromLatLng
                    }
                }
            }

        }
        return null
    }

    fun isAllInBaseMarker(
        parent: MutableCollection<ClusterItem>?,
        child: MarkerSingle?
    ): Boolean {
        if (parent == null || child == null) {
            return false
        }

        child.stationDetail.id?.let { targetId ->
            parent.filter {
                (it as StationClusterItem).stationDetail.id != null
            }.map {
                (it as StationClusterItem).stationDetail.id!!
            }.contains(targetId).let {
                return it
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setPrevData(prevCluster: MutableList<BaseMarkerData>) {
        prev = prevCluster
    }

}
