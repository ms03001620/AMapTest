package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.polestar.repository.data.charging.StationDetail

class MarkerAction(val map: MapProxy) {
    fun addMarker(stationDetail: StationDetail): Marker? {
        return map.addMarker(stationDetail)
    }

    fun getMarker(stationDetail: StationDetail) = map.getMarker(stationDetail)

    fun transfer(from: StationDetail, to: StationDetail, removeAtEnd: Boolean) {
        map.getMarker(from)?.let {
            transfer(from.id, it, stationDetailToLatLng(to), removeAtEnd)
        }
    }

    fun transfer(id: String?, marker: Marker, moveTo: LatLng, removeAtEnd: Boolean) {
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(1000)
            if(removeAtEnd){
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

}