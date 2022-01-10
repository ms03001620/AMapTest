package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.example.amaptest.logd
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import com.polestar.repository.data.charging.StationDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerMockViewModel : ViewModel() {
    private lateinit var stationsList: List<StationDetail>

    val p = lazy {
        JsonTestUtil.mock(stationsList.subList(0, 4))
    }

    val c = lazy{
        JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 3),
            stationsList.subList(3, 4)
        )
    }
    val noChangeLiveData = SingleLiveEvent<MutableList<BaseMarkerData>>()
    val onAnimTaskLiveData = SingleLiveEvent<AnimTaskData>()

    fun loadDefault(context: Context, file: String, start: Int, end: Int) {
        stationsList = AssetsReadUtils.mockStation(context, file)!!
        logd("loadDefault", "MarkerMockViewModel")
    }

    var distanceInfo : DistanceInfo?=null

    fun calcClusters(distanceInfo: DistanceInfo) {
        logd("distanceInfo:${distanceInfo.toString()}", "MarkerMockViewModel")
        if (this.distanceInfo == null) {
            logd("first ", "MarkerMockViewModel")
            this.distanceInfo = distanceInfo
            noChangeLiveData.value = p.value
        } else {
            logd("first ", "MarkerMockViewModel")
            ClusterUtils.process(p.value, c.value)

            //TODO NodeTrack -> AnimTaskData
        }
    }



}