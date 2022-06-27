package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import com.polestar.base.utils.logv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.LinkedBlockingQueue

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
        mapProxy.clear()
        mapProxy.createMarkers(data)
    }

    fun processNodeList(clusterAnimData: ClusterAnimData) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                lock.acquire()
                logv("start task:$clusterAnimData", "AnimFactory")

                unSafeProcessNodeList(clusterAnimData)
                if (clusterAnimData.isAnimTaskEmpty()) {
                    lock.release()
                }
            } catch (e: Exception) {
                loge("restore static markers ${clusterAnimData.currentNode.size}", "AnimFactory", e)
                mapProxy.clear()
                mapProxy.createMarkers(clusterAnimData.currentNode)

                lock.release()
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

        val queue = LinkedBlockingQueue<Marker>()

        clusterAnimData.animTask.forEach { nodeTrack ->
            if (nodeTrack.isExpTask) {
                processExpTask(nodeTrack.node, nodeTrack, queue)
            } else {
                processCospTask(nodeTrack, queue)
            }
        }

        if (clusterAnimData.animTask.isNotEmpty() && queue.isEmpty()) {
            assert(false)
        }

        while (queue.isNotEmpty()) {
            queue.poll()?.startAnimation()
        }
    }

    private fun processExpTask(
        curr: BaseMarkerData,
        nodeTrack: ClusterUtils.NodeTrack,
        queue: LinkedBlockingQueue<Marker>
    ) {
        nodeTrack.subNodeNoMove?.let { subNode ->
            val marker =
                mapProxy.getMarker(subNode.parentId) ?: throw IllegalArgumentException("data error")

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

                queue.add(marker)
            }
        }

        nodeTrack.subNodeList.forEach { subNode ->
            val id = subNode.subNode.getId()

            var marker = mapProxy.getMarker(id)

            if (marker == null) {
                marker = mapProxy.createMarker(subNode.subNode, subNode.parentLatLng)
            }

            if (marker != null) {
                transfer(marker, curr.getLatlng(), false, null)
                queue.add(marker)
            } else {
                loge("createMarker null:${subNode.subNode.getStation()?.id}", "logicException")
                assert(false)
            }
        }
    }

    private fun processCospTask(
        nodeTrack: ClusterUtils.NodeTrack,
        queue: LinkedBlockingQueue<Marker>
    ) {
        nodeTrack.subNodeNoMove?.let { subNode ->
            if (subNode.nodeType == ClusterUtils.NodeType.PIECE) {
                // animId 7
                mapProxy.getMarker(subNode.parentId)?.let {
                    mapProxy.updateMarker(it, subNode.subNode)
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
                throw IllegalArgumentException("processCospTask marker = null")
            } else {
                val isLastIndex = index == (size - 1)
                val li = if (isLastIndex) object : Animation.AnimationListener {
                    override fun onAnimationStart() {
                    }

                    override fun onAnimationEnd() {
                        // 动画后创建或更新聚合点
                        val node = nodeTrack.subNodeNoMove
                        if (node != null) {
                            mapProxy.getMarker(node.subNode.getId())?.let {
                                mapProxy.updateMarker(it, nodeTrack.node)
                            }
                        } else {
                            mapProxy.createMarker(nodeTrack.node)
                        }
                    }
                } else null

                transfer(marker, nodeTrack.node.getLatlng(), true, li)
                queue.add(marker)
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
        //marker.startAnimation()
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