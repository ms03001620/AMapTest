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
        fun exp(removed: MutableList<BaseMarkerData>, map: HashMap<LatLng, MutableList<BaseMarkerData>>)

        /**
         * 合拢，added合拢后形成的新节点， map LatLng合拢节点的终点， list各自的起点
         * 子节点从各自节点通过动画移动到合拢节点，消失，然后创建合拢节点
         */
        fun cosp(map: HashMap<LatLng, MutableList<BaseMarkerData>>, added: MutableList<BaseMarkerData>)
    }

    var prev: MutableList<BaseMarkerData>? = null
    var lastZoom = 0f


    fun process(set: MutableList<BaseMarkerData>?, zoom: Float) {
        val isZoomIn = zoom> lastZoom
        lastZoom = zoom

        set?.let {
            if (prev == null || isSameData(prev!!, set)) {
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

    fun queue(set: MutableList<BaseMarkerData>?, zoom: Float) {
        val start = System.currentTimeMillis()
        process(set, zoom)
        logd("queue spend:${System.currentTimeMillis() - start}")
    }

    private fun processZoomOut(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            val collapsed = createCollapsedTask(it, curr)
            action?.cosp(collapsed, curr)
        }
    }

    fun processZoomIn(curr: MutableList<BaseMarkerData>) {
        prev?.let {
            val exp = createExpTask(it, curr)
            val removed = createRemoveTask(it, curr)
            action?.exp(removed, exp)
        }
    }

    fun isSameData(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): Boolean {
        if (prev.size == curr.size) {
            val clusterPrev = prev.filterIsInstance<MarkerCluster>()
            val clusterCurr = curr.filterIsInstance<MarkerCluster>()

            if(clusterPrev.size == clusterCurr.size){
                return true
            }
        }
        return false
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
        prev.forEach { prevElement ->
            val fromLatLng = prevElement.getLatlng()
            when (prevElement) {
                is MarkerCluster -> {
                    when (target) {
                        is MarkerSingle -> {
                            if (isAllInBaseMarker(prevElement.list.items, target)) {
                                return fromLatLng
                            }
                        }
                        is MarkerCluster -> {
                            if (isAllInTarget(prevElement.list.items, target.list.items)) {
                                return fromLatLng
                            }
                        }
                    }
                }
                is MarkerSingle -> {
                    if (prevElement.getId() == target.getId()) {
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
