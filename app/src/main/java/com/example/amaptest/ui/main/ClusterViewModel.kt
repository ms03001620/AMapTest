package com.example.amaptest.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.*
import com.polestar.charging.ui.cluster.distance.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClusterViewModel : ViewModel() {
    private val algorithm: BaseClusterAlgorithm = DistanceQuadTreeAlgorithm()
    private var prevDistanceInfo: DistanceInfo? = null

    val clusterLiveData = MutableLiveData<Set<Cluster<ClusterItem>>>()

    fun mock(context: Context, distanceInfo: DistanceInfo, filename: String) {
        prevDistanceInfo = distanceInfo
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, filename)?.let {
                it.map {
                    StationClusterItem(it)
                }.let {
                    algorithm.feed(it)
                    algorithm.calc(distanceInfo, callback = {
                        clusterLiveData.postValue(it)
                    })
                }
            }
        }
    }

    fun createCluster(distanceInfo: DistanceInfo) {
        if (prevDistanceInfo?.same(distanceInfo) == true) {
            logd("distanceMerge: skip: $distanceInfo")
            return // skip same
        }
        prevDistanceInfo = distanceInfo
        viewModelScope.launch {
            algorithm.calc(distanceInfo, callback = {
                clusterLiveData.postValue(it)
            })
        }
    }

}