package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng
import com.example.amaptest.logd
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.base.StationClusterItem

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
        val pieces = mutableListOf<Piece>()
        prevList.forEach { old ->
            val latLngPrev = old.getLatlng()!!
            // 新的是否包含了全部老的数据
            if (isAllIn(new, old)) {
                var pieceType = PieceType.FULL_PARTY
                if (old.getSize() == 1) {
                    pieceType = PieceType.SINGLE
                }
                pieces.add(Piece(latLngPrev, pieceType, old))
            } else {
                // 老的部分在新的中
                val pieceType = PieceType.PARTY
                val items  = findItems(new.getCluster().items, old.getCluster().items)
                if (items?.isNotEmpty()==true) {
                    pieces.add(
                        Piece(
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
     * node 最新点
     * piece Latlng 老点坐标。BaseMarkerData 数据
     */
    data class NodeTrack(val node: BaseMarkerData, val pieces: MutableList<Piece>)

    enum class PieceType {
        SINGLE, FULL_PARTY, PARTY
    }

    data class Piece(val latLngPrev: LatLng, val pieceType: PieceType, val baseMarkerData: BaseMarkerData)


    fun findSameLatlngNode(node: BaseMarkerData, lastList: MutableList<BaseMarkerData>) =
        lastList.firstOrNull{it.getLatlng() == node.getLatlng()}

    fun isGoneNode(node: BaseMarkerData, lastList: MutableList<BaseMarkerData>) =
        findSameLatlngNode(node, lastList) == null
}