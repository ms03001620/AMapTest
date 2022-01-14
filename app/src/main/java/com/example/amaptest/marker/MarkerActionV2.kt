package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd
import java.lang.UnsupportedOperationException

class MarkerActionV2(val mapProxy: MapProxy) {

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        addMarkers(data)
    }

    fun addMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        mapProxy.createMarkers(baseMarkerDataList)
    }

    fun processNodeList(list: List<ClusterUtils.NodeTrack>) {
        list.map {
            it.node.getLatlng()!!
        }.let {
            mapProxy.removeAllMarker(it)
        }

        list
         /*   .subList(0, 1)*/
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
            // 自有一个任务的track 默认为展开任务
            processSubNode(curr, nodeTrack.subNodeList.first())
        } else {
            processMultiNode(nodeTrack)
        }
    }

    fun processSubNode(curr: BaseMarkerData, subNode: ClusterUtils.SubNode) {
        if (ClusterUtils.isSamePosition(curr.getLatlng(), subNode.parentLatLng)) {
            // 子点和目标点一致。讲
            // update
            val m = mapProxy.getMarker(subNode.parentId)
            assert(m != null)
            mapProxy.updateMarker(marker = m!!, curr)
        } else {
            attemptTransfer(
                baseMarkerData = subNode.subNode,
                autoCreatePosition = subNode.parentLatLng,
                moveTo = curr.getLatlng()!!
            )
        }
    }

    fun processMultiNode(nodeTrack: ClusterUtils.NodeTrack) {
        // nodeTrack 为多子任务， 子任务合并成该点
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
                baseMarkerData = subNode.subNode,
                autoCreatePosition = subNode.parentLatLng,
                moveTo = curr.getLatlng()!!,
                listener = if (index == 0) listener else null
            )
        }
    }

    fun cospTransfer(
        baseMarkerData: BaseMarkerData,
        moveTo: LatLng,
        autoCreate: Boolean = true,
        autoCreatePosition: LatLng? = null,
        listener: Animation.AnimationListener? = null
    ) {
        var marker = mapProxy.getMarker(baseMarkerData)

        if (autoCreate && marker == null) {
            val createPosition = autoCreatePosition ?: baseMarkerData.getLatlng()
            marker = mapProxy.createMarker(baseMarkerData, createPosition)
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