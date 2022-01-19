package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.ClusterItem

class ClusterAdapter(val action: OnClusterAction? = null) {
    interface OnClusterAction {
        fun noChange(data: MutableList<BaseMarkerData>)
        fun onAnimTask(animTaskData: AnimTaskData)
    }

    private var prev: MutableList<BaseMarkerData>? = null

    fun process(curr: MutableList<BaseMarkerData>) {
        if (prev == null || isSameData(prev, curr)) {
            logd("same", "process noChange")
            action?.noChange(curr)
        } else {
            val subPrev = prev!!.toMutableList()
            val subCurr = curr.toMutableList()

            ClusterUtils.delSame(subPrev, subCurr)
            assert(ClusterUtils.getMarkerListSize(subPrev) == ClusterUtils.getMarkerListSize(subCurr))
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

    fun queue(set: MutableList<BaseMarkerData>) {
        val start = System.currentTimeMillis()
        process(set)
        logd("queue spend:${System.currentTimeMillis() - start}")
    }

    fun isSameData(
        prev: MutableList<BaseMarkerData>?,
        curr: MutableList<BaseMarkerData>
    ) = curr == prev

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
            //curr中有一个数据和node一致
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
            //   AB12 ->   a1, b2
            // 当前节点内所有数据 都在某个老节点中
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
                            // target所有数据都在 clusterItem中
                            if (isClusterContainerItems(clusterItem, target.list.items)) {
                                //clusterItem中包含target全部数据            //TODO 可能只包含部分节点
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

    fun findItems(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ) = child?.filter {
        parent?.contains(it) == true
    }

    fun isClusterContainerItems(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ) = ClusterUtils.isAllItemInParent(parent, child)
}
