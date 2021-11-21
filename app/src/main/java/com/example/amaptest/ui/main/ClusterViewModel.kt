package com.example.amaptest.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.logd
import com.example.amaptest.ui.main.calc.Cluster
import com.example.amaptest.ui.main.calc.DistanceInfo
import com.example.amaptest.ui.main.calc.same
import kotlinx.coroutines.launch

class ClusterViewModel : ViewModel() {
    lateinit var clusterCalc: ClusterCalcDemoBase
    val stationClusterLiveData = MutableLiveData<List<Cluster>>()
    lateinit var lastDistanceMerge: DistanceInfo

    fun mock(context: Context, distanceInfo: DistanceInfo) {
        lastDistanceMerge = distanceInfo
        viewModelScope.launch {
            AssetsReadUtils.mockStation(context)?.let {
                clusterCalc.setData(it)
                clusterCalc.calc(distanceInfo, callback = {
                    stationClusterLiveData.postValue(it)
                })
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
            clusterCalc.calc(distanceInfo, callback = {
                stationClusterLiveData.postValue(it)
            })
        }
    }

    fun initClusterAlgorithm(clusterIconSize: Float) {
        clusterCalc = ClusterCalcDemoV2()
    }
}