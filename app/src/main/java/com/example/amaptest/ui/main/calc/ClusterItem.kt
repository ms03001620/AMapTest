package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng

interface ClusterItem<T> {
    fun getPosition(): LatLng

    fun getEntry(): T
}