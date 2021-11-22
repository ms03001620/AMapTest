package com.example.amaptest.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.*
import com.example.amaptest.ui.main.quadtree.Cluster
import com.example.amaptest.ui.main.quadtree.ClusterItem
import kotlinx.coroutines.launch

class ClusterViewModel : ViewModel() {
    lateinit var clusterCalcClusterAlgorithm: BaseClusterAlgorithm
    val stationClusterLiveData = MutableLiveData<Set<Cluster<ClusterItem>>>()
    lateinit var lastDistanceMerge: DistanceInfo

    fun mock(context: Context, distanceInfo: DistanceInfo) {
        lastDistanceMerge = distanceInfo
        viewModelScope.launch {
            AssetsReadUtils.mockStation(context)?.let {
                it.map {
                    StationClusterItem(it)
                }.let {
                    clusterCalcClusterAlgorithm.feed(it)
                    clusterCalcClusterAlgorithm.calc(distanceInfo, callback = {
                        stationClusterLiveData.postValue(it)
                    })
                }
            }
        }
    }

    fun reCalcCluster(distanceInfo: DistanceInfo) {
        if (lastDistanceMerge.same(distanceInfo)) {
            logd("distanceMerge: skip: $distanceInfo")
            return // skip same
        }
        lastDistanceMerge = distanceInfo
        viewModelScope.launch {
            clusterCalcClusterAlgorithm.calc(distanceInfo, callback = {
                stationClusterLiveData.postValue(it)
            })
        }
    }

    fun initClusterAlgorithm(clusterIconSize: Float) {
        //clusterCalcClusterAlgorithm = DistanceAlgorithm()
        clusterCalcClusterAlgorithm = DistanceQuadTreeAlgorithm()
    }
}