package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.ClusterItem
import kotlin.math.abs

object ClusterUtils {
    fun process(prev: MutableList<BaseMarkerData>, curr: MutableList<BaseMarkerData>): List<NodeTrack> {
        val result = curr.map {
            createTrackData(it, prev)
        }
        println(result)
        return result
    }

    fun processAndDeSame(prev: MutableList<BaseMarkerData>, curr: MutableList<BaseMarkerData>): List<NodeTrack> {
        val subPrev = prev.toMutableList()
        val subCurr = curr.toMutableList()

        delSame(subPrev, subCurr)

        val result = subCurr.map {
            createTrackData(it, subPrev)
        }

        return result
    }

    /**
     * 生成当前数据的组成结构NodeTrack。 依据prevList历史数据生成
     * curr 当前数据
     * prevList 全部历史数据
     */
    fun createTrackData(curr: BaseMarkerData, prevList: MutableList<BaseMarkerData>): NodeTrack {
        val subNodeList = mutableListOf<SubNode>()
        prevList.forEach { prev ->
            val latLngPrev = prev.getLatlng()!!
            // 新的是否包含了全部老的数据
            // ABCD AB true
            // A ABCD
            if (isClusterContainerItems(curr.getCluster().items, prev.getCluster().items)) {
                var nodeType = NodeType.PARTY
                if (prev.getSize() == 1) {
                    nodeType = NodeType.SINGLE
                }
                subNodeList.add(SubNode(latLngPrev, nodeType, prev))

            } else if (isClusterContainerItems(prev.getCluster().items, curr.getCluster().items)) {
                var nodeType = NodeType.PARTY
                if (curr.getSize() == 1) {
                    nodeType = NodeType.SINGLE
                }
                subNodeList.add(SubNode(latLngPrev, nodeType, curr))
            } else {
                // 老的部分在新的中
                val nodeType = NodeType.PIECE
                val items = findItems(curr.getCluster().items, prev.getCluster().items)
                if (items?.isNotEmpty() == true) {
                    subNodeList.add(
                        SubNode(
                            latLngPrev,
                            nodeType,
                            MarkerDataFactory.create(items, latLngPrev)
                        )
                    )
                }
            }
        }
        return NodeTrack(curr, subNodeList)
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
    ): Boolean {
        if ((child?.size ?: 0) > (parent?.size ?: 0)) {
            return false
        }
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

    fun delSame(
        prev: MutableList<BaseMarkerData>,
        curr: MutableList<BaseMarkerData>
    ){
        val prevCopy = prev.toMutableList()
        prev.removeAll(curr)
        curr.removeAll(prevCopy)
    }

    fun isSamePosition(a: LatLng?, b: LatLng?, error: Float = 0.000001f): Boolean {
        if (a == null) {
            return false
        }
        if (b == null) {
            return false
        }

        val v1 = abs(a.latitude - b.latitude)
        val v2 = abs(a.longitude - b.longitude)
        return v1 < error && v2 < error
    }

    /**
     * node 节点
     * subNode 组成该节点的子节点
     */
    data class NodeTrack(val node: BaseMarkerData, val subNodeList: MutableList<SubNode>)

    /**
     * SINGLE, 独立
     * PARTY, 全同
     * PIECE, 混杂
     */
    enum class NodeType {
        SINGLE, PARTY, PIECE
    }

    /**
     * parentLatLng 子节点 所在簇坐标（如果之前是在别的簇中，取之前簇坐标，于其自身坐标不一样）
     * nodeType 节点性质， 除了混杂数据是当前簇数据的一部分，其余都是当前簇
     * subNode 节点数据
     */
    data class SubNode(
        val parentLatLng: LatLng,
        val nodeType: NodeType,
        val subNode: BaseMarkerData
    )
}