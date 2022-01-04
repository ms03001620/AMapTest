package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.ClusterItem

object ClusterUtils {
    fun process(prev: MutableList<BaseMarkerData>, curr: MutableList<BaseMarkerData>) {
        curr.forEach {
            createTrackData(it, prev)
        }
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
            if (isClusterContainerItems(curr.getCluster().items, prev.getCluster().items)) {
                var pieceType = NodeType.FULL_PARTY
                if (prev.getSize() == 1) {
                    pieceType = NodeType.SINGLE
                }
                subNodeList.add(SubNode(latLngPrev, pieceType, prev))
            } else {
                // 老的部分在新的中
                val pieceType = NodeType.PARTY
                val items = findItems(curr.getCluster().items, prev.getCluster().items)
                if (items?.isNotEmpty() == true) {
                    subNodeList.add(
                        SubNode(
                            latLngPrev,
                            pieceType,
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

    /**
     * node 节点
     * subNode 组成该节点的子节点
     */
    data class NodeTrack(val node: BaseMarkerData, val subNodeList: MutableList<SubNode>)

    /**
     * SINGLE, 独立
     * FULL_PARTY, 全同
     * PARTY, 混杂
     */
    enum class NodeType {
        SINGLE, FULL_PARTY, PARTY
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