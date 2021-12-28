package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkerActionViewModel : ViewModel() {
    val clustersLiveData = MutableLiveData<MutableList<BaseMarkerData>>()


    val noChangeLiveData = SingleLiveEvent<MutableList<BaseMarkerData>>()
    val onClusterCreateAndMoveTo = SingleLiveEvent<Pair<MutableList<BaseMarkerData>, HashMap<LatLng, MutableList<BaseMarkerData>>>>()
    val onClusterMoveToAndRemove = SingleLiveEvent<Pair<MutableList<BaseMarkerData>, HashMap<LatLng, MutableList<BaseMarkerData>>>>()

    private val adapter = ClusterAdapter(object: ClusterAdapter.OnClusterAction{
        override fun noChange(data: MutableList<BaseMarkerData>) {
            noChangeLiveData.postValue(data)
        }

        override fun onClusterCreateAndMoveTo(
            removed: MutableList<BaseMarkerData>,
            map: HashMap<LatLng, MutableList<BaseMarkerData>>
        ) {
            onClusterCreateAndMoveTo.postValue(Pair(removed, map))
        }

        override fun onClusterMoveToAndRemove(
            map: HashMap<LatLng, MutableList<BaseMarkerData>>,
            added: MutableList<BaseMarkerData>
        ) {
            onClusterMoveToAndRemove.postValue(Pair(added, map))
        }
    })

    private val clusterAlgorithm by lazy {
        //AlgorithmWallpaper(DistanceAlgorithm())
        AlgorithmWallpaper(DistanceQuadTreeAlgorithm())
    }


/*    fun loadDefault(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, "json_stations570.json")?.let { data->
                data.map {
                    StationClusterItem(it)
                }.let {
                    clusterAlgorithm.feed(it.subList(22, 34))
                    //clusterAlgorithm.feed(it)
                }
            }
        }
    }*/

    fun loadDefault(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            AssetsReadUtils.mockStation(context, "json_stations8.json")?.let { data->
                data.map {
                    StationClusterItem(it)
                }.let {
                    clusterAlgorithm.feed(it)
                }
            }
        }
    }

/*    fun calcClusters(distanceInfo: DistanceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            clusterAlgorithm.calc(distanceInfo) {
                clustersLiveData.postValue(MarkerDataFactory.create(it))
            }
        }
    }*/

    fun calcClusters(distanceInfo: DistanceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            clusterAlgorithm.calc(distanceInfo) {
                adapter.queue(MarkerDataFactory.create(it), distanceInfo.cameraPosition?.zoom ?: 0f)
            }
        }
    }

}