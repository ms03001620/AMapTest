package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import kotlin.math.abs

class MarkerActionV2(val mapProxy: MapProxy) {

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        addMarkers(data)
    }

    fun addMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        mapProxy.createMarkers(baseMarkerDataList)
    }

    fun remove(remove: MutableList<BaseMarkerData>) {
        mapProxy.removeMarkers(remove)
    }

    fun makeAnim(list: List<ClusterUtils.NodeTrack>) {
        val size = list.size
        list.forEach {
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
        nodeTrack.subNodeList.forEach {
            processSubNode(curr, it)
        }

    }

    fun processSubNode(curr: BaseMarkerData, subNode: ClusterUtils.SubNode) {
        attemptTransfer(
            baseMarkerData = subNode.subNode,
            autoCreatePosition = subNode.parentLatLng,
            moveTo = curr.getLatlng()!!
        )
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