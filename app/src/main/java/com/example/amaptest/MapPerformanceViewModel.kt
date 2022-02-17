package com.example.amaptest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.marker.*
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapPerformanceViewModel : ViewModel() {

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }

    val dataLiveData = SingleLiveEvent<MutableList<BaseMarkerData>?>()

    fun loadDefault(context: Context, file: String, start: Int, end: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, file)?.let { data ->
                val stationCenter = data[0]

                stationCenter.let {
                    val list = ArrayList<StationDetail>()
                    repeat(10) { i ->
                        repeat(10) { j ->
                            stationCenter.copy(
                                id = "id$i$j",
                                lat = stationCenter.lat?.plus(0.001 * i),
                                lng = stationCenter.lng?.plus(0.001 * j),
                                acTotal = i * 10 + j
                            ).let {
                                list.add(it)
                            }
                        }
                    }

                    list.map {
                        MarkerSingle(it, it.toLatLng())
                    }.let {
                        dataLiveData.postValue(it.toMutableList())
                    }
                }
            }
        }
    }

}