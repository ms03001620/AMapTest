package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerActionViewModel : ViewModel() {
    val clustersLiveData = MutableLiveData<MutableList<BaseMarkerData>>()

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }


    fun loadDefault(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, "json_stations570.json")?.let { data->
                data.map {
                    StationClusterItem(it)
                }.let {
                    //clusterAlgorithm.feed(it.subList(0, 8))
                    clusterAlgorithm.feed(it)
                }
            }
        }
    }

    fun calcClusters(distanceInfo: DistanceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            clusterAlgorithm.calc(distanceInfo) {
                clustersLiveData.postValue(MarkerDataFactory.create(it))
            }
        }
    }

}