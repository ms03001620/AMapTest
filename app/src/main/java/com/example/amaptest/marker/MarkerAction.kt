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

class MarkerAction(val map: MapProxy) {

    fun setList(data: MutableList<BaseMarkerData>) {
        map.clear()
        addCluster(data)
    }

    fun addCluster(baseMarkerDataList: MutableList<BaseMarkerData>): List<Marker?> {
        return baseMarkerDataList.map {
            when(it){
                is MarkerSingle -> addMarker(it.stationDetail)
                is MarkerCluster -> addCluster(it)
                else -> null
            }
        }
    }

    fun addCluster(cluster: MarkerCluster, redirectLatLng: LatLng? = null): Marker? {
        val latLng = redirectLatLng ?: cluster.getLatlng()
        return map.createOrUpdateCluster(cluster.getId(), cluster.getSize(), latLng)
    }

    fun addMarker(stationDetail: StationDetail): Marker? {
        return map.createMarker(stationDetail)
    }

    fun addMarker(stationDetail: StationDetail, latLng: LatLng): Marker? {
        return map.createMarker(stationDetail, latLng)
    }

    fun transfer(from: StationDetail, to: StationDetail, removeAtEnd: Boolean) {
        map.getMarker(from)?.let {
            transfer(from.id, it, stationDetailToLatLng(to), removeAtEnd)
        }
    }

    fun transfer(from: BaseMarkerData, to: LatLng, removeAtEnd: Boolean, listener: Animation.AnimationListener? = null) {
        when (from) {
            is MarkerCluster -> transfer(from, to, removeAtEnd, listener)
            is MarkerSingle -> transfer(from.stationDetail, to, removeAtEnd, listener)
        }
    }

    fun transfer(from: StationDetail, to: LatLng, removeAtEnd: Boolean, listener: Animation.AnimationListener? = null) {
        map.getMarker(from)?.let {
            transfer(from.id, it, to, removeAtEnd, listener)
        }
    }

    fun transfer(from: MarkerCluster, to: LatLng, removeAtEnd: Boolean, listener: Animation.AnimationListener? = null) {
        map.getMarker(from)?.let {
            transfer(from.getId(), it, to, removeAtEnd, listener)
        }
    }

    fun removed(removed: MutableList<BaseMarkerData>) {
        removed.forEach { removedItem ->
            when (removedItem) {
                is MarkerCluster -> {
                    map.deleteMarker(removedItem.getId())
                }
            }
        }
    }

    fun cosp(map: HashMap<LatLng, MutableList<BaseMarkerData>>, added: MutableList<BaseMarkerData>) {
        map.forEach {
            val toLatLng = it.key
            it.value.forEach { itemCluster ->
                transfer(itemCluster, toLatLng, true, object : Animation.AnimationListener{
                    override fun onAnimationStart() {
                    }
                    override fun onAnimationEnd() {
                        addCluster(added)
                    }
                })
            }
        }
    }

    fun exp(removed: MutableList<BaseMarkerData>,map: HashMap<LatLng, MutableList<BaseMarkerData>>) {
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
                                itemCluster.stationDetail,
                                itemCluster.stationDetail.toLatLng(),
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    fun transfer(id: String?, marker: Marker, moveTo: LatLng, removeAtEnd: Boolean, listener: Animation.AnimationListener? = null) {
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(CLUSTER_MOVE_ANIM)
            if (removeAtEnd) {
                this.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart() {
                    }

                    override fun onAnimationEnd() {
                        map.deleteMarker(id)
                        listener?.onAnimationEnd()
                    }
                })
            }
        })
        marker.setAnimation(set)
        marker.startAnimation()
    }

    fun delete(stationDetail: StationDetail) {
        map.deleteMarker(stationDetail)
    }

    fun stationDetailToLatLng(stationDetail: StationDetail) =
        LatLng(stationDetail.lat ?: Double.NaN, stationDetail.lng ?: Double.NaN)


    companion object {
        const val CLUSTER_MOVE_ANIM = 300L
    }
}