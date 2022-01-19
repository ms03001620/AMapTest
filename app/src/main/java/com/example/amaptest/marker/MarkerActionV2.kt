package com.example.amaptest.marker

import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd

class MarkerActionV2(val mapProxy: MapProxy1) {

    fun clear() {
        mapProxy.clear()
    }

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        addMarkers(data)
    }

    fun addMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        mapProxy.createMarkers(baseMarkerDataList)
    }

    fun processNodeList(pair: Pair<List<ClusterUtils.NodeTrack>, List<BaseMarkerData>>) {
        pair.second.map {
            it.getLatlng()
        }.let {
            mapProxy.removeMarkers(it)
        }

        pair.first
            //.subList(0, 1)
            .forEach {
                processNode(it)
            }
    }

    /**
     * node 节点
     * subNode 组成该节点的子节点
     */
    //data class NodeTrack(val node: BaseMarkerData, val subNodeList: MutableList<ClusterUtils.SubNode>)
    fun processNode(nodeTrack: ClusterUtils.NodeTrack) {
        val curr = nodeTrack.node
        if (nodeTrack.subNodeList.size == 1) {
            processNodeToSub(curr, nodeTrack.subNodeList.first())
        } else {
            processSubToNode(nodeTrack)
        }
    }

    fun processNodeToSub(curr: BaseMarkerData, subNode: ClusterUtils.SubNode) {
        if (ClusterUtils.isSamePosition(curr.getLatlng(), subNode.parentLatLng)) {
            // 子点和目标点一致。讲
            val m = mapProxy.getMarker(subNode.parentLatLng)
            if (m == null) {
                mapProxy.createMarker(curr)
            } else {
                mapProxy.updateMarker(m, curr)
            }
        } else {
            attemptTransfer(subNode, curr.getLatlng())
/*            Handler(Looper.getMainLooper()).postDelayed( {
                attemptTransfer(subNode,curr.getLatlng())
            }, 10)*/
        }
    }

    fun processSubToNode(nodeTrack: ClusterUtils.NodeTrack) {
        val curr = nodeTrack.node
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart() {
            }

            override fun onAnimationEnd() {
                // 找到子点中与合并后点相同的点，在其他动画播放后 讲改点修改为合并后的点
                val sameNode = nodeTrack.subNodeList.firstOrNull { subNode ->
                    ClusterUtils.isSamePosition(
                        curr.getLatlng(),
                        subNode.subNode.getLatlng()
                    )
                }

                // 移动后更新相同点为curr
                if (sameNode != null) {
                    val marker = mapProxy.getMarker(sameNode.subNode.getLatlng())
                    if (marker != null) {
                        mapProxy.updateMarker(marker, curr)
                    } else {
                        mapProxy.createMarker(curr)
                        // 未找到的原因是这个点的id 无法获取
                        logd("未找到的原因是这个点的id 无法获取", "MarkerActionV2")
                    }
                } else {
                    //移动后创建curr
                    mapProxy.createMarker(nodeTrack.node)
                }
            }
        }

        nodeTrack.subNodeList.filterNot { subNode ->
            ClusterUtils.isSamePosition(curr.getLatlng(), subNode.subNode.getLatlng())
        }.forEachIndexed { index, subNode ->
            cospTransfer(
                subNode,
                moveTo = curr.getLatlng(),
                listener = if (index == 0) listener else null
            )
        }
    }

    fun cospTransfer(
        subNode: ClusterUtils.SubNode,
        moveTo: LatLng,
        listener: Animation.AnimationListener? = null
    ) {
        val baseMarkerData = subNode.subNode

        var marker: Marker? = null
        if (subNode.nodeType == ClusterUtils.NodeType.PIECE) {
            marker = mapProxy.createMarker(baseMarkerData, subNode.parentLatLng)
        } else {
            marker = mapProxy.getMarker(baseMarkerData)
        }

        if (marker == null) {
            marker = mapProxy.createMarker(baseMarkerData, subNode.parentLatLng)
        }

        assert(marker != null)

        // transfer marker
        transfer(baseMarkerData, moveTo, true, listener)
    }

    fun attemptTransfer(
        subNode: ClusterUtils.SubNode,
        moveTo: LatLng,
    ) {
        val baseMarkerData = subNode.subNode
        val autoCreatePosition = subNode.parentLatLng
        val marker = mapProxy.createMarker(baseMarkerData, autoCreatePosition)
        logd("attemptTransfer:$marker", "______")

        marker?.let {
            if (ClusterUtils.isSamePosition(moveTo, marker.position)) {
                // keep marker
            } else {
                // transfer marker
                transfer(marker, moveTo, false, null)
            }
        } ?: run {
            assert(marker != null)
        }
    }

    private fun transfer(
        baseMarkerData: BaseMarkerData,
        moveTo: LatLng,
        removeAtEnd: Boolean = false,
        listener: Animation.AnimationListener? = null
    ) {
        val marker = mapProxy.getMarker(baseMarkerData)
        transfer(marker!!, moveTo, removeAtEnd, listener)
    }

    private fun transfer(
        marker: Marker,
        moveTo: LatLng,
        removeAtEnd: Boolean = false,
        listener: Animation.AnimationListener? = null
    ) {
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(CLUSTER_MOVE_ANIM)
            this.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart() {
                    listener?.onAnimationStart()
                }

                override fun onAnimationEnd() {
                    if (removeAtEnd) {
                        mapProxy.removeMarker(marker)
                    }
                    listener?.onAnimationEnd()
                }
            })
        })
        marker.setAnimation(set)
        marker.startAnimation()
    }

    fun transfer(marker: Marker, moveTo: LatLng) {
        val startPos = marker.position
        logd("before transfer pos:${startPos} to $moveTo", "_____")
        TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(CLUSTER_MOVE_ANIM)
            this.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart() {
                }

                override fun onAnimationEnd() {
                    val endPos = marker.position
                    val deltaLat = endPos.latitude - moveTo.latitude
                    val deltaLng = endPos.longitude - moveTo.longitude

                    logd("after deltaLat:${deltaLat}, deltaLng:${deltaLng}, same:${ClusterUtils.isSamePosition(endPos, moveTo)} ", "_____")
                }
            })
        }.let {
            val set = AnimationSet(true)
            set.addAnimation(it)
            set
        }.let {
            marker.setAnimation(it)
            marker.startAnimation()
        }
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 2000L
    }
}