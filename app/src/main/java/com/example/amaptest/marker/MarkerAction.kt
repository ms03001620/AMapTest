package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd
import com.polestar.repository.data.charging.toLatLng
import kotlin.math.abs

class MarkerAction(val mapProxy: MapProxy) {

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        addMarkers(data)
    }

    fun addMarkers(baseMarkerDataList: MutableList<BaseMarkerData>) {
        mapProxy.createMarkers(baseMarkerDataList)
    }

    fun addMarker(baseMarkerData: BaseMarkerData, latLng: LatLng?): Marker {
        return mapProxy.createOrUpdateMarkerToPosition(baseMarkerData, latLng)
    }

    fun remove(remove: MutableList<BaseMarkerData>) {
        mapProxy.removeMarkers(remove)
    }

    fun onAnimTaskLiveData(animTaskData: AnimTaskData) {
        collapsed(animTaskData)
        expansion(animTaskData)
    }

    /**
     * 展开，removed原节点消失， map latLng原节点地址 list各自终点
     * 原来节点先消失，然后从改节点分裂出子节点，并通过动画移动到各自终点（终点如果已存在则更新最终节点）
     */
    fun expansion(
        animTaskData: AnimTaskData
    ) {
        remove(animTaskData.deleteList)
        animTaskData.expList.forEach {
            val fromLatLng = it.key
            it.value.forEach { itemCluster ->
                when (itemCluster) {
                    is MarkerCluster -> {
                        val mark = addMarker(itemCluster, fromLatLng)
                        // cluster不存在，创建并播放动画
                        itemCluster.getStation()?.let { station ->
                            mapProxy.getMarker(itemCluster)?.let {
                                transfer(itemCluster.getId(), it, station.toLatLng(), false)
                            }
                        }
                    }
                    is MarkerSingle -> {
                        val t = addMarker(itemCluster, fromLatLng)
                        logd("MarkerSingle t:$t")
                        mapProxy.getMarker(itemCluster)?.let {
                            transfer(
                                itemCluster.getId(),
                                it,
                                itemCluster.stationDetail.toLatLng(),
                                false
                            )
                        }
                    }
                }
            }
        }
    }


    /**
     * 合拢，added合拢后形成的新节点， map LatLng合拢节点的终点， list各自的起点
     * 子节点从各自节点通过动画移动到合拢节点，消失，然后创建合拢节点
     */
    fun collapsed(animTaskData: AnimTaskData) {
        val map = animTaskData.cospList
        val added = animTaskData.addList

        if (added.isNotEmpty()) {
            assert(map.isNotEmpty())
        }

        var total = 0
        map.forEach {
            total += it.value.size
        }

        val anim = object : Animation.AnimationListener {
            override fun onAnimationStart() {
            }

            override fun onAnimationEnd() {
                total--
                //然后创建合拢节点
                if (total == 0) {
                    addMarkers(added)
                }
            }
        }

        map.forEach {
            //合拢节点
            val toLatLng = it.key
            it.value.forEach { itemCluster ->
                mapProxy.getMarker(itemCluster)?.let {
                    transfer(itemCluster.getId(), it, toLatLng, true, anim)
                }
            }
        }
    }

    fun transfer(
        id: String?,
        marker: Marker,
        moveTo: LatLng,
        removeAtEnd: Boolean,
        listener: Animation.AnimationListener? = null
    ) {
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(CLUSTER_MOVE_ANIM)
            if (removeAtEnd) {
                this.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart() {
                    }

                    override fun onAnimationEnd() {
                        mapProxy.removeMarker(id)
                        listener?.onAnimationEnd()
                    }
                })
            }
        })
        marker.setAnimation(set)
        marker.startAnimation()
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
            marker = mapProxy.createOrUpdateMarkerToPosition(baseMarkerData, createPosition)
        }

        assert(marker != null)

        if (isSamePosition(moveTo, marker?.position)) {
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
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(/*CLUSTER_MOVE_ANIM*/2000)
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


    fun isSamePosition(a: LatLng?, b: LatLng?, error: Float = 0.000001f): Boolean {
        if (a == null) {
            return false
        }
        if (b == null) {
            return false
        }

        val v1 = abs(a.latitude - b.latitude)
        val v2 = abs(a.longitude - b.longitude)
        return v1 < error && v2 < error
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 300L
    }
}