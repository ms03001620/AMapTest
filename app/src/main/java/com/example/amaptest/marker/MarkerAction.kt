package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.example.amaptest.marker.ClusterUtils.isExpTask
import com.polestar.base.utils.logd

class MarkerAction(val mapProxy: MapProxy) {

    fun clear() {
        mapProxy.clear()
    }

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        mapProxy.createMarkers(data)
    }

    fun processNodeList(clusterAnimData: ClusterAnimData) {
        // animId 5
        clusterAnimData.deleteList.forEach {
            mapProxy.removeMarker(it.getId())
        }

        clusterAnimData.animTask.forEach { nodeTrack ->
            if (nodeTrack.isExpTask()) {
                processExpTask(nodeTrack.node, nodeTrack.subNodeList.first())
            } else {
                processCospTask(nodeTrack)
            }
        }
    }

    private fun processExpTask(curr: BaseMarkerData, subNode: ClusterUtils.SubNode) {
        if (subNode.isNoMove) {
            val marker = mapProxy.getMarker(subNode.parentId)
            if (marker == null) {
                mapProxy.createMarker(curr)
            } else {
                mapProxy.updateMarker(marker, curr)
            }
        } else {
            mapProxy.createMarker(subNode.subNode, subNode.parentLatLng)?.let {
                transfer(it, curr.getLatlng(), false, null)
            } ?: run {
                assert(false)
            }
        }
    }

    private fun processCospTask(nodeTrack: ClusterUtils.NodeTrack) {
        val curr = nodeTrack.node
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart() {
            }

            override fun onAnimationEnd() {
                // 动画后创建或更新聚合点
                val subNode = nodeTrack.subNodeList.firstOrNull { it.isNoMove }

                if (subNode != null) {
                    mapProxy.getMarker(subNode.subNode.getId())?.let {
                        mapProxy.updateMarker(it, curr)
                    }
                } else {
                    mapProxy.createMarker(nodeTrack.node)
                }
            }
        }

        var isFirst = true
        nodeTrack.subNodeList.forEach { subNode ->
            if (subNode.isNoMove) {
                if (subNode.nodeType == ClusterUtils.NodeType.PIECE) {
                    // animId 7
                    val marker = mapProxy.getMarker(subNode.parentId)
                    if (marker != null) {
                        mapProxy.updateMarker(marker, subNode.subNode)
                    }
                } else {
                    // animId 1; 合并任务中，子点已在合并点，不需要移动。
                }
            } else {
                // 合并任务，移动子点到合并点，并且删除
                val baseMarkerData = subNode.subNode
                logd("cospTransfer nodeType:${subNode.nodeType}", "______")

                val marker = if (subNode.nodeType == ClusterUtils.NodeType.PREV_IN_CURR) {
                    mapProxy.getMarker(baseMarkerData.getId())
                } else {
                    mapProxy.createMarker(baseMarkerData, subNode.parentLatLng)
                }

                marker?.let {
                    transfer(marker, curr.getLatlng(), true, if (isFirst) listener else null)
                    isFirst = false
                } ?: run {
                    assert(false)
                }
            }
        }
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
                        mapProxy.removeMarker(marker.title)
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

    fun printMarkers() {
        val markers = mapProxy.getAllMarkers()
        logd("list marker size:${markers.size}", "_____")
        markers.forEach {
            logd("list marker:${it.title}", "_____")
        }
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 2000L
    }
}