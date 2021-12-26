package com.example.amaptest.marker

import android.util.Log
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

    fun addList(data: MutableList<BaseMarkerData>) {
        data.forEach {
            when (it) {
                is MarkerCluster -> {
                    addCluster(it)
                }
                is MarkerSingle -> {
                    addCluster(it)
                }
            }
        }
    }

    fun addCluster(markerSingle: MarkerSingle): Marker? {
        return addMarker(markerSingle.stationDetail)
    }

    fun addCluster(cluster: MarkerCluster): Marker? {
        return map.addCluster(cluster.getId(), cluster.getSize(), cluster.getLatlng())
    }

    fun addCluster(cluster: MarkerCluster, latLng: LatLng): Marker? {
        return map.addCluster(cluster.getId(), cluster.getSize(), latLng)
    }

    fun addMarker(stationDetail: StationDetail): Marker? {
        return map.addMarker(stationDetail)
    }

    fun addMarker(stationDetail: StationDetail, latLng: LatLng): Marker? {
        return map.addMarker(stationDetail, latLng)
    }

    fun getMarker(stationDetail: StationDetail) = map.getMarker(stationDetail)

    fun getMarker(cluster: MarkerCluster) = map.getMarker(cluster)

    fun transfer(from: StationDetail, to: StationDetail, removeAtEnd: Boolean) {
        map.getMarker(from)?.let {
            transfer(from.id, it, stationDetailToLatLng(to), removeAtEnd)
        }
    }

    fun transfer(from: StationDetail, to: LatLng, removeAtEnd: Boolean) {
        map.getMarker(from)?.let {
            transfer(from.id, it, to, removeAtEnd)
        }
    }

    fun transfer(from: MarkerCluster, to: LatLng, removeAtEnd: Boolean) {
        map.getMarker(from)?.let {
            transfer(from.getId(), it, to, removeAtEnd)
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

    fun exp(map: HashMap<LatLng, MutableList<BaseMarkerData>>) {
        map.forEach {
            val fromLatLng = it.key
            it.value.forEach { itemCluster ->
                when (itemCluster) {
                    is MarkerCluster -> {
                        val mark = addCluster(itemCluster, fromLatLng)
                        if (mark == null) {
                            // cluster已存在，刷新
                            this.map.stationToClusterOptions(
                                itemCluster.getSize(),
                                itemCluster.getLatlng()!!
                            ).let {
                                getMarker(itemCluster)?.setMarkerOptions(it)
                            }
                        } else {
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

        println(map)

    }

    fun transfer(id: String?, marker: Marker, moveTo: LatLng, removeAtEnd: Boolean) {
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