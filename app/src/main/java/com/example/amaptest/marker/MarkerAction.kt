package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.lang.IllegalArgumentException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MarkerAction(val mapProxy: MapProxy) {
    private val lock = AnimFactory(Semaphore(1))

    fun clear() {
        mapProxy.clear()
    }

    fun clearMarker(prev: MutableList<BaseMarkerData>?) {
        prev?.map {
            it.getId()
        }?.let {
            mapProxy.clearMarker(it)
        }
    }

    fun setList(data: MutableList<BaseMarkerData>) {
        postSyncTask {
            mapProxy.clear()
            mapProxy.createMarkers(data)
            lock.forceRelease()
        }
    }

    fun processNodeList(clusterAnimData: ClusterAnimData) {
        postSyncTask {
            logd("start task:$clusterAnimData", "AnimFactory")
            unSafeProcessNodeList(clusterAnimData)
            if (clusterAnimData.isAnimTaskEmpty()) {
                val result = lock.forceRelease()
                if (result.not()) {
                    loge("unSafeProcessNodeList autoRelease false", "logicException")
                }
            }
        }
    }

    private fun postSyncTask(task: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            lock.acquire()
            task.invoke()
        }
    }

    private fun unSafeProcessNodeList(clusterAnimData: ClusterAnimData) {
        logd("do task: ${clusterAnimData.getInfoString()}")
        // animId 5
        clusterAnimData.deleteList.forEach {
            mapProxy.removeMarker(it.getId())
        }

        clusterAnimData.animTask.forEach { nodeTrack ->
            if (nodeTrack.isExpTask) {
                processExpTask(nodeTrack.node, nodeTrack)
            } else {
                processCospTask(nodeTrack)
            }
        }
    }

    private fun processExpTask(curr: BaseMarkerData, nodeTrack: ClusterUtils.NodeTrack) {
        nodeTrack.subNodeNoMove?.let { subNode ->
            val marker = mapProxy.getMarker(subNode.parentId) ?: throw IllegalArgumentException("data error")

            if (ClusterUtils.isSamePosition(marker.position, curr.getLatlng())) {
                mapProxy.updateMarker(marker, curr)
            } else {
                transfer(
                    marker,
                    curr.getLatlng(),
                    false,
                    object : Animation.AnimationListener {
                        override fun onAnimationStart() {
                        }

                        override fun onAnimationEnd() {
                            mapProxy.updateMarker(marker, curr)
                        }
                    })
            }
        }

        nodeTrack.subNodeList.forEach { subNode ->
            val marker = mapProxy.createMarker(subNode.subNode, subNode.parentLatLng)
            assert(marker != null)
            transfer(marker!!, curr.getLatlng(), false, null)
        }
    }

    private fun processCospTask(nodeTrack: ClusterUtils.NodeTrack) {
        nodeTrack.subNodeNoMove?.let { subNode ->
            if (subNode.nodeType == ClusterUtils.NodeType.PIECE) {
                // animId 7
                val marker = mapProxy.getMarker(subNode.parentId)
                if (marker != null) {
                    // 相同的断言来自于数据逻辑中这里不会出错 fixPosition将会是必要的
                    assert(ClusterUtils.isSamePosition(marker.position, subNode.subNode.getLatlng()))
                    mapProxy.updateMarker(marker, subNode.subNode)
                }
            } else {
                // animId 1; 合并任务中，子点已在合并点，不需要移动。
            }
        }

        val size = nodeTrack.subNodeList.size
        nodeTrack.subNodeList.forEachIndexed { index, subNode ->
            // 合并任务，移动子点到合并点，并且删除
            val baseMarkerData = subNode.subNode

            val marker = if (subNode.nodeType == ClusterUtils.NodeType.PREV_IN_CURR) {
                mapProxy.getMarker(baseMarkerData.getId())
            } else {
                mapProxy.createMarker(baseMarkerData, subNode.parentLatLng)
            }

            if (marker == null) {
                throw IllegalArgumentException("data error")
            }

            val isLastIndex = index == (size - 1)
            val li = if (isLastIndex) object : Animation.AnimationListener {
                override fun onAnimationStart() {
                }

                override fun onAnimationEnd() {
                    // 动画后创建或更新聚合点
                    val subNode = nodeTrack.subNodeNoMove
                    if (subNode != null) {
                        mapProxy.getMarker(subNode.subNode.getId())?.let {
                            mapProxy.updateMarker(it, nodeTrack.node)
                        }
                    } else {
                        mapProxy.createMarker(nodeTrack.node)
                    }
                }
            } else null

            transfer(marker, nodeTrack.node.getLatlng(), true, li)
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
            this.setAnimationListener(
                lock.createAnimationListener(
                    object : Animation.AnimationListener {
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
            )
        })
        marker.setAnimation(set)
        marker.startAnimation()
    }


    fun printMarkers() {
        val markers = mapProxy.getAllMarkers()
        logd("list marker size:${markers.size}", "MarkerAction")
        markers.forEach {
            logd("list marker:${it.title}", "MarkerAction")
        }
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 200L
    }
}