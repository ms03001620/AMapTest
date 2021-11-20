package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng

object ClusterChangeHelper {

    data class History(val cluster: Cluster, val target: LatLng)


    fun changeCalc(old: List<Cluster>, new: List<Cluster>) {

    }
}