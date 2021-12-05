package com.example.amaptest.marker

import android.view.animation.AccelerateInterpolator
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.animation.AlphaAnimation
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation

class MarkerAction {
    fun addMarker(markerOptions: MarkerOptions, map: AMap) {
        map.addMarker(markerOptions)
    }

    fun transfer(marker: Marker, moveTo: LatLng) {
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(moveTo).apply {
            this.setInterpolator(AccelerateInterpolator())
            this.setDuration(1000)
        })

        marker.setAnimation(set)
        marker.startAnimation()
    }

    fun delete(marker: Marker) {
        marker.remove()
    }
}