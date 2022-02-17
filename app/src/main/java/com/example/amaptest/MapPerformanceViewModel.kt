package com.example.amaptest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.marker.AlgorithmWallpaper
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapPerformanceViewModel : ViewModel() {

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }


    fun loadDefault(context: Context, file: String, start: Int, end: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, file)?.let { data ->
                data./*filter {
                    isCloseToPosition(it.toLatLng())
                }.*/map {
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

}