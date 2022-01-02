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
        JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))
    }

    val c = lazy{
        JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(1, 2).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(3, 4).first()
            )
        )
    }

    val noChangeLiveData = SingleLiveEvent<MutableList<BaseMarkerData>>()
    val onAnimTaskLiveData = SingleLiveEvent<AnimTaskData>()

    private val adapter = ClusterAdapter(object : ClusterAdapter.OnClusterAction {
        override fun noChange(data: MutableList<BaseMarkerData>) {
            noChangeLiveData.postValue(data)
        }

        override fun onAnimTask(animTaskData: AnimTaskData) {
            onAnimTaskLiveData.postValue(animTaskData)
        }
    })

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }

    fun loadDefault(context: Context, file: String, start: Int, end: Int) {
        stationsList = AssetsReadUtils.mockStation(context, file)!!

        adapter.queue(p.value)
    }

    fun calcClusters(distanceInfo: DistanceInfo) {
        logd(distanceInfo.toString(), "zoom")
        if(distanceInfo.cameraPosition?.zoom == 16f){
            adapter.queue(c.value)
        }
        if(distanceInfo.cameraPosition?.zoom == 15f){
            adapter.queue(p.value)
        }

    }



}