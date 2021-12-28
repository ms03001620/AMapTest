package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.base.utils.logd
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng
import java.lang.UnsupportedOperationException

class MarkerAction(val mapProxy: MapProxy) {

    fun setList(data: MutableList<BaseMarkerData>) {
        mapProxy.clear()
        addCluster(data)
    }

    fun addCluster(baseMarkerDataList: MutableList<BaseMarkerData>) {
        baseMarkerDataList.forEach {
            when (it) {
                is MarkerSingle -> addMarker(it.stationDetail)
                is MarkerCluster -> addCluster(it)
            }
        }
    }

    fun addCluster(cluster: MarkerCluster, redirectLatLng: LatLng? = null): Marker? {
        val latLng = redirectLatLng ?: cluster.getLatlng()
        return mapProxy.createOrUpdateCluster(cluster.getId(), cluster.getSize(), latLng)
    }

    fun addMarker(stationDetail: StationDetail, latLng: LatLng? = null): Marker? {
        return mapProxy.createMarker(stationDetail, latLng)
    }

    fun transfer(
        from: BaseMarkerData,
        to: LatLng,
        removeAtEnd: Boolean,
        listener: Animation.AnimationListener? = null
    ) {
        mapProxy.getMarker(from)?.let {
            transfer(from.getId(), it, to, removeAtEnd, listener)
        }
    }

    fun removed(removed: MutableList<BaseMarkerData>) {
        removed.forEach {
            if(it is MarkerSingle){
                throw UnsupportedOperationException("new case")
            }
            mapProxy.deleteMarker(it.getId())
        }
    }

    /**
     * 合拢，added合拢后形成的新节点， map LatLng合拢节点的终点， list各自的起点
     * 子节点从各自节点通过动画移动到合拢节点，消失，然后创建合拢节点
     */
    fun cosp(pair: Pair<HashMap<LatLng, MutableList<BaseMarkerData>>, MutableList<BaseMarkerData>>) {
        val map = pair.first
        val added = pair.second
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
                    addCluster(added)
                }
            }
        }

        map.forEach {
            //合拢节点
            val toLatLng = it.key
            it.value.forEach { itemCluster ->

                logd("___ ${itemCluster.getLatlng()}, to $toLatLng, ${itemCluster.javaClass.simpleName}")

                transfer(
                    itemCluster,
                    toLatLng,
                    true/*移动到合拢节点，消失*/,
                    anim
                )
            }
        }
    }

    /**
     * 展开，removed原节点消失， map latLng原节点地址 list各自终点
     * 原来节点先消失，然后从改节点分裂出子节点，并通过动画移动到各自终点（终点如果已存在则更新最终节点）
     */
    fun exp(
        removed: MutableList<BaseMarkerData>,
        map: HashMap<LatLng, MutableList<BaseMarkerData>>
    ) {
        removed(removed)
        map.forEach {
            val fromLatLng = it.key
            it.value.forEach { itemCluster ->
                when (itemCluster) {
                    is MarkerCluster -> {
                        val mark = addCluster(itemCluster, fromLatLng)
                        if (mark != null) {
                            // cluster不存在，创建并播放动画
                            itemCluster.getStation()?.let {
                                transfer(itemCluster, it.toLatLng(), false)
                            }
                        }
                    }
                    is MarkerSingle -> {
                        val t = addMarker(itemCluster.stationDetail, fromLatLng)
                        logd("MarkerSingle t:$t")
                        if (t != null) {
                            transfer(
                                itemCluster,
                                itemCluster.stationDetail.toLatLng(),
                                false
                            )
                        }
                    }
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
                        mapProxy.deleteMarker(id)
                        listener?.onAnimationEnd()
                    }
                })
            }
        })
        marker.setAnimation(set)
        marker.startAnimation()
    }

    companion object {
        const val CLUSTER_MOVE_ANIM = 300L
    }
}