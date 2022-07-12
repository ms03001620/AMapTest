package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.ClusterItem
import kotlin.math.abs
import kotlin.math.roundToInt

object ClusterUtils {
    fun createClusterAnimData(subPrev: MutableList<BaseMarkerData>, subCurr: MutableList<BaseMarkerData>, zoom: Float): ClusterAnimData {
        val prev = subPrev.toMutableList()
        val curr = subCurr.toMutableList()
        delSame(prev, curr)

        val taskList = curr.map { createTrackData(it, prev) }
        val deleteList = createDeleteData(prev, curr, taskList)
        return ClusterAnimData(taskList, deleteList, zoom, subCurr)
    }

    fun createClusterNoAnimData(subPrev: MutableList<BaseMarkerData>, subCurr: MutableList<BaseMarkerData>): ClusterData {
        val prev = subPrev.toMutableList()
        val curr = subCurr.toMutableList()
        delSame(prev, curr)

        val updateNodeList = mutableListOf<ClusterUpdateData>()
        val createNodeList = mutableListOf<BaseMarkerData>()
        val deleteNodeList = mutableListOf<BaseMarkerData>()

        prev.forEach { prevNode ->
            if (isPositionNotExist(prevNode, curr)) {
                deleteNodeList.add(prevNode)
            }
        }

        curr.forEach { currNode ->
            if (isPositionNotExist(currNode, prev)) {
                createNodeList.add(currNode)
            } else {
                val newPos = currNode.getLatlng()
                var id: String? = null

                prev.forEach {
                    if (isSamePosition(newPos, it.getLatlng())) {
                        id = it.getId()
                    }
                }
                assert(id != null)
                id?.let {
                    updateNodeList.add(ClusterUpdateData(it, currNode))
                }
            }
        }

        return ClusterData(updateNodeList, createNodeList, deleteNodeList)
    }

    fun isPositionNotExist(data: BaseMarkerData, target: MutableList<BaseMarkerData>): Boolean {
        target.forEach { element ->
            if (isSamePosition(element.getLatlng(), data.getLatlng())) {
                return false //exits
            }
        }
        return true // no exits
    }

    fun isNoSameHashCode(list: List<BaseMarkerData>): Boolean {
        val set = LinkedHashSet<BaseMarkerData>()

        list.forEach {
            if (set.contains(it)) {
                return false
            } else {
                set.add(it)
            }
        }
        return true
    }

    fun calcFinger(markerCluster: MarkerCluster):Pair<MarkerCluster, Int> {
        val itemValueNotOrder = markerCluster.getCluster().items.sumOf {
            it.id.hashCode()
        }
        return Pair(markerCluster, itemValueNotOrder)
    }

    fun createDeleteData(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>,
        taskList: List<NodeTrack>
    ): List<BaseMarkerData> {
        val del = prev.filter { p ->
            curr.firstOrNull { isSamePosition(it.getLatlng(), p.getLatlng()) } == null
        }

        // 过滤删除任务（有些点需要在动画后删除）
        return del.filterNot { delData ->
            var findDel = false
            run lit@{
                taskList.forEach { track ->
                    if (track.subSize() > 1) {
                        track.subNodeNoMove?.let { subNode ->
                            // task合并任务已包含该删除点，该点会在合并结束后删除
                            if (subNode.subNode == delData) {
                                findDel = true
                                return@lit
                            }
                        }

                        track.subNodeList.forEach { subNode ->
                            // task合并任务已包含该删除点，该点会在合并结束后删除
                            if (subNode.subNode == delData) {
                                findDel = true
                                return@lit
                            }
                        }
                    }
                }
            }
            findDel
        }
    }

    /**
     * 生成当前数据的组成结构NodeTrack。 依据prevList历史数据生成
     * curr 当前数据
     * prevList 全部历史数据
     */
    fun createTrackData(curr: BaseMarkerData, prevList: MutableList<BaseMarkerData>): NodeTrack {
        val subNodeList = mutableListOf<SubNode>()
        var subNodeListNoMove: SubNode? = null
        prevList.forEach { prev ->
            val latLngPrev = prev.getLatlng()
            val idPrev = prev.getId()
            val isSamePos = isSamePosition(prev.getLatlng(), curr.getLatlng())
            // 新的是否包含了全部老的数据
            // ABCD AB true
            // A ABCD
            if (isAllItemInParent(curr.getCluster().items, prev.getCluster().items)) {
                // 新点包括所有老点
                val nodeType = NodeType.PREV_IN_CURR
                if (isSamePos) {
                    subNodeListNoMove = SubNode(latLngPrev, idPrev, nodeType, prev, isSamePos)
                } else {
                    subNodeList.add(SubNode(latLngPrev, idPrev, nodeType, prev, isSamePos))
                }

            } else if (isAllItemInParent(prev.getCluster().items, curr.getCluster().items)) {
                // 老点包括所有新点
                val nodeType = NodeType.CURR_IN_PREV
                if (isSamePos) {
                    subNodeListNoMove = SubNode(latLngPrev, idPrev,nodeType, curr, isSamePos)
                }else{
                    subNodeList.add(SubNode(latLngPrev, idPrev,nodeType, curr, isSamePos))
                }

            } else {
                // 老的部分在新的中
                val nodeType = NodeType.PIECE
                val items = findItems(curr.getCluster().items, prev.getCluster().items)
                if (items?.isNotEmpty() == true) {
                    if (isSamePos) {
                        subNodeListNoMove =
                            SubNode(
                                latLngPrev,
                                idPrev,
                                nodeType,
                                MarkerDataFactory.create(items, latLngPrev),
                                isSamePos
                            )

                    } else {
                        subNodeList.add(
                            SubNode(
                                latLngPrev,
                                idPrev,
                                nodeType,
                                MarkerDataFactory.create(items, latLngPrev),
                                isSamePos
                            )
                        )
                    }
                }
            }
        }

        val subNodeListNoMoveSize = if (subNodeListNoMove == null) 0 else 1

        val isExpTask = (subNodeList.size + subNodeListNoMoveSize) == 1

        return NodeTrack(curr, subNodeList, subNodeListNoMove, isExpTask)
    }

    fun findItems(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ) = child?.filter {
        parent?.contains(it) == true
    }


    fun getMarkerListSize(list: MutableList<BaseMarkerData>): Int {
        var total = 0
        list.forEach {
            total += it.getSize()
        }
        return total
    }

    /**
     * parent包含了全部的child元素
     */
    fun isAllItemInParent(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ): Boolean {
        if (child == null || parent==null) {
            return false
        }
        val sizeParent = parent.size
        val sizeChild = child.size

        if (sizeParent < sizeChild) {
            return false
        }

        val parentIds = parent.map { it.id }
        val childIds = child.map { it.id }

        return parentIds.containsAll(childIds)
    }

    fun delSame(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ){
        val prevCopy = prev.toMutableList()
        prev.removeAll(curr)
        curr.removeAll(prevCopy)
    }

    /**
     * 是否是相同点
     * 判断经纬度误差小于delta值
     */
    fun isSamePosition(a: LatLng?, b: LatLng?, delta: Float = 5.0E-6f, showDiff: Boolean = false): Boolean {
        //  5.0E-6f ,marker移动到指定pos后，计算移动后的marker pos 和指定的值误差
        if (a == null) {
            return false
        }
        if (b == null) {
            return false
        }

        val v1 = abs(a.latitude - b.latitude)
        val v2 = abs(a.longitude - b.longitude)
        if (showDiff) {
            logd("latitude diff:${v1.toBigDecimal()}(${v1 < delta}), longitude diff:${v2.toBigDecimal()}(${v2 < delta})")
        }
        return v1 < delta && v2 < delta
    }

    fun loops(start: Float, end: Float, step: Float, callback: (first: Float, second: Float) -> Unit) {
        if (end <= start) {
            throw UnsupportedOperationException("end <= start !!")
        }
        if (step > (end-start)) {
            throw IllegalArgumentException("step to bigger !!")
        }
        var s = start
        var e = start
        var option = 0
        while (true) {
            if (option == 0) {
                e += step
            } else {
                e -= step
            }
            if (e.compareTo(end) > 0) {
                option = 1
                e -= step
                continue
            }
            if (e.compareTo(start) < 0) {
                break
            }
            callback(s.round(1), e.round(1))
            s = e
        }
    }

    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0f
        repeat(decimals) { multiplier *= 10 }
        return ((this * multiplier).roundToInt() / multiplier)
    }

    /**
     * @node 新节点
     * @subNodeList 数据的来源
     * @subNodeListNoMove  数据的来源，未移动
     * @isExpTask 是否是展开任务
     */
    class NodeTrack(
        val node: BaseMarkerData,
        val subNodeList: MutableList<SubNode>,
        val subNodeNoMove: SubNode?,
        val isExpTask: Boolean
    )

    fun NodeTrack.subSize() = subNodeList.size + if(subNodeNoMove==null) 0 else 1

        /**
     * @PREV_IN_CURR 子节点全部被合并 A,B,CD -> ABCD
     * @CURR_IN_PREV 子节点被分裂 ABCD -> A,B,CD
     * @PIECE， 子节点只是部分数据，不完整
     */
    enum class NodeType {
        PREV_IN_CURR, CURR_IN_PREV, PIECE
    }

    /**
     * @parentLatLng 子节点的父节点坐标
     * @parentId 子节点的父节点id
     * @nodeType 子节点性质
     * @subNode 子节点数据
     */
    data class SubNode(
        val parentLatLng: LatLng,
        val parentId: String,
        val nodeType: NodeType,
        val subNode: BaseMarkerData,
        val isNoMove: Boolean
    )
}