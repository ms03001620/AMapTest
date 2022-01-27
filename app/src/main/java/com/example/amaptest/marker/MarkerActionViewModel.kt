package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerActionViewModel : ViewModel() {
    val noChangeLiveData = SingleLiveEvent<MutableList<BaseMarkerData>>()
    val clusterAnimDataLiveData = SingleLiveEvent<ClusterAnimData>()


    var distanceInfo: DistanceInfo? = null
    var prev: MutableList<BaseMarkerData>? = null

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }

    fun loadDefault(context: Context, file: String, start: Int, end: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, file)?.let { data ->
                data.map {
                    StationClusterItem(it)
                }.let {
                    if (start != -1 && end != -1) {
                        clusterAlgorithm.feed(it.subList(start, end))
                    } else {
                        clusterAlgorithm.feed(it)
                    }
                    postCalcClusters()
                }
            }
        }
    }

    fun calcClusters(distanceInfo: DistanceInfo) {
        logd("calcClusters: ${distanceInfo.zoomLevel}", "_____")
        this.distanceInfo = distanceInfo
        postCalcClusters()
    }

    private fun postCalcClusters() {
        if (clusterAlgorithm.isFeed().not()) {
            //logd("not feed", "_____")
            return
        }

        if (distanceInfo == null) {
            //logd("not distanceInfo", "_____")
            return
        }

        distanceInfo?.let { distanceInfo ->
            viewModelScope.launch(Dispatchers.IO) {
                clusterAlgorithm.calc(distanceInfo) {
                    val curr = MarkerDataFactory.create(it)

                    prev?.let {
                        val p = ClusterUtils.createClusterAnimData(it, curr)
                        clusterAnimDataLiveData.postValue(p)
                    } ?: run {
                        noChangeLiveData.postValue(curr)
                    }

                    prev = curr.toMutableList()
                }
            }
        }
    }

    fun printPrev() {
        logd("prev size:${prev?.size}", "_____")
        prev?.forEach {
            logd("list marker:${it.getId()}", "_____")
        }
    }
}