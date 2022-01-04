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
     * 为新数据，生成所有老数据的信息
     */
    fun createTrackData(new: BaseMarkerData, prevList: MutableList<BaseMarkerData>): NodeTrack{
        val pieces = mutableListOf<SubNode>()
        prevList.forEach { old ->
            val latLngPrev = old.getLatlng()!!
            // 新的是否包含了全部老的数据
            if (isAllIn(new, old)) {
                var pieceType = NodeType.FULL_PARTY
                if (old.getSize() == 1) {
                    pieceType = NodeType.SINGLE
                }
                pieces.add(SubNode(latLngPrev, pieceType, old))
            } else {
                // 老的部分在新的中
                val pieceType = NodeType.PARTY
                val items  = findItems(new.getCluster().items, old.getCluster().items)
                if (items?.isNotEmpty()==true) {
                    pieces.add(
                        SubNode(
                            latLngPrev,
                            pieceType,
                            MarkerDataFactory.create(items, latLngPrev)
                        )
                    )
                }
            }
        }
        return NodeTrack(new, pieces)
    }

    fun findItems(
        parent: MutableCollection<ClusterItem>?,
        child: MutableCollection<ClusterItem>?
    ) = child?.filter {
        parent?.contains(it) == true
    }

    fun isAllIn(new: BaseMarkerData, old: BaseMarkerData) =
        isClusterContainerItems(new.getCluster().items, old.getCluster().items)

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

    enum class NodeType {
        SINGLE, FULL_PARTY, PARTY
    }

    data class SubNode(val parentLatLng: LatLng, val nodeType: NodeType, val subNode: BaseMarkerData)


    fun findSameLatlngNode(node: BaseMarkerData, lastList: MutableList<BaseMarkerData>) =
        lastList.firstOrNull{it.getLatlng() == node.getLatlng()}

    fun isGoneNode(node: BaseMarkerData, lastList: MutableList<BaseMarkerData>) =
        findSameLatlngNode(node, lastList) == null
}