package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerActionViewModel : ViewModel() {
    val noChangeLiveData = SingleLiveEvent<MutableList<BaseMarkerData>>()
    val onAnimTaskLiveData = SingleLiveEvent<Pair<List<ClusterUtils.NodeTrack>, List<BaseMarkerData>>>()



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
                }
            }
        }
    }

    fun calcClusters(distanceInfo: DistanceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            clusterAlgorithm.calc(distanceInfo) {
                ssss(MarkerDataFactory.create(it))
            }
        }
    }

    var prev :MutableList<BaseMarkerData>?=null

    fun ssss(curr: MutableList<BaseMarkerData>) {
        if (prev == null) {
            noChangeLiveData.postValue(curr)
        } else {
            val p = ClusterUtils.processCreateDel(prev!!, curr)

            onAnimTaskLiveData.postValue(p)
        }
        val mutableList = mutableListOf<BaseMarkerData>()
        mutableList.addAll(curr)
        prev = mutableList
    }
}