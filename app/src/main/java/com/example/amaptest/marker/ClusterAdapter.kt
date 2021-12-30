package com.example.amaptest.marker

import androidx.annotation.VisibleForTesting
import com.amap.api.maps.model.LatLng
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.ClusterItem
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

    /*
     * p(0,3) c(0,1  1,2  2,3)
     * p(0,3) c(0,2  2,3)
     * p(0,4) c(0,2  2,4)
     */
    fun createAnimTaskData(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ): AnimTaskData {
        val addList = mutableListOf<BaseMarkerData>()
        val deleteList = mutableListOf<BaseMarkerData>()
        val cospList = hashMapOf<LatLng, MutableList<BaseMarkerData>>()
        val expList = hashMapOf<LatLng, MutableList<BaseMarkerData>>()

        prev.forEach { node ->
            val latLng = findLatLng(curr, node)
            if (latLng == null) {
                // node, 最新结果中已经找不到老的点，它已经分裂。加入删除任务
                deleteList.add(node)
            } else {
                // node 最近结果中包含了该点，加入合并任务
                findOrCreateClusterList(cospList, latLng).add(node)
            }
        }

        curr.forEach { node ->
            val latLng = findLatLng(prev, node)
            if (latLng == null) {
                addList.add(node)
            } else {
                // 新节点是包含在老节点内的数据, 加入展开任务
                findOrCreateClusterList(expList, latLng).add(node)
            }
        }

        val emptyTask = cospList.size == 0 && expList.size == 0
        assert(emptyTask.not())

        return AnimTaskData(addList, deleteList, cospList, expList)
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
            result = mutableListOf()
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
            // list
            when (baseMarkerData) {
                is MarkerCluster -> {
                    val clusterItem = baseMarkerData.list.items
                    when (target) {
                        // cluster in cluster
                        is MarkerCluster -> {
                            if (isListInList(clusterItem, target.list.items)) {
                                return fromLatLng
                            }
                        }
                        // single in cluster
                        is MarkerSingle -> {
                            if (isMarkSingleInList(clusterItem, target)) {
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

    fun isMarkSingleInList(
        list: MutableCollection<ClusterItem>?,
        markerSingle: MarkerSingle
    ): Boolean {
        return list?.map { it.id }?.contains(markerSingle.stationDetail.id) == true
    }

    fun isListInList(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ): Boolean {
        child?.map {
            it.id
        }?.let { ids ->
            parent?.map {
                it.id
            }?.containsAll(ids).let {
                return it == true
            }
        }
        return false
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setPrevData(prevCluster: MutableList<BaseMarkerData>) {
        prev = prevCluster
    }

}
