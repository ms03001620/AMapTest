package com.example.amaptest.marker

import androidx.annotation.VisibleForTesting
import com.amap.api.maps.model.LatLng
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.ClusterItem
import kotlin.collections.HashMap

class ClusterAdapter(val action: OnClusterAction? = null) {

    interface OnClusterAction {
        fun noChange(data: MutableList<BaseMarkerData>)
        fun onAnimTask(animTaskData: AnimTaskData)
    }

    var prev: MutableList<BaseMarkerData>? = null

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
            installTask(subPrev, subCurr)
        }
        prev = curr
    }

    fun installTask(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ){
        action?.onAnimTask(createAnimTaskData(prev, curr))
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
