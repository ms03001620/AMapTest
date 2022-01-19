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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MarkerActionV2(val mapProxy: MapProxy) {

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
            it.getLatlng()!!
        }.let {
            mapProxy.removeAllMarker(it)
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
                //logd("11111111a", "______")
                mapProxy.createMarker(curr)
            } else {
                //logd("11111111b", "______")
                mapProxy.updateMarker(m, curr)
            }
        } else {
            attemptTransfer(
                baseMarkerData = subNode.subNode,
                autoCreatePosition = subNode.parentLatLng,
                moveTo = curr.getLatlng()!!
            )
/*            Handler(Looper.getMainLooper()).postDelayed( {
                attemptTransfer(
                    baseMarkerData = subNode.subNode,
                    autoCreatePosition = subNode.parentLatLng,
                    moveTo = curr.getLatlng()!!
                )
            }, 50)*/
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
                    val marker = mapProxy.getMarker(sameNode.subNode.getLatlng()!!)
                    if (marker != null) {
                        mapProxy.updateMarker(marker = marker, curr)
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
                moveTo = curr.getLatlng()!!,
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
        baseMarkerData: BaseMarkerData,
        moveTo: LatLng,
        autoCreate: Boolean = true,
        autoCreatePosition: LatLng? = null,
        removeAtEnd: Boolean = false,
        listener: Animation.AnimationListener? = null
    ) {
        var marker = mapProxy.getMarker(baseMarkerData)

        logd("attemptTransfer:$marker", "______")
        if (autoCreate && marker == null) {
            val createPosition = autoCreatePosition ?: baseMarkerData.getLatlng()
            marker = mapProxy.createMarker(baseMarkerData, createPosition)
        }

        assert(marker != null)

        if (ClusterUtils.isSamePosition(moveTo, marker?.position)) {
            // keep marker
            listener?.onAnimationStart()
            if (removeAtEnd) {
                mapProxy.removeMarker(baseMarkerData.getId())
            }
            listener?.onAnimationEnd()
        } else {
            // transfer marker
            transfer(baseMarkerData, moveTo, removeAtEnd, listener)
        }
    }

    private fun transfer(
        baseMarkerData: BaseMarkerData,
        moveTo: LatLng,
        removeAtEnd: Boolean = false,
        listener: Animation.AnimationListener? = null
    ) {
        val marker = mapProxy.getMarker(baseMarkerData)
        assert(marker != null)
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
                        mapProxy.removeMarker(baseMarkerData.getId())
                    }
                    listener?.onAnimationEnd()
                }
            })
        })
        marker?.setAnimation(set)
        marker?.startAnimation()
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 2000L
    }
}