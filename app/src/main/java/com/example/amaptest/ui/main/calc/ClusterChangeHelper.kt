package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail

object ClusterChangeHelper {

    data class History(val cluster: Cluster, val target: LatLng)


    /**
     *   group -> groups
     *   group -> points
     *
     *   point -> group
     *
     */
    fun changeCalc(old: List<Cluster>, new: List<Cluster>):List<History> {
        val result = mutableListOf<History>()

        old.forEach {


            new.forEach {


                it.clusterItem
            }
        }

        return result
    }

}