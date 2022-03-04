package com.example.amaptest.marker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.charging.ui.cluster.base.sameZoom
import com.polestar.charging.ui.cluster.distance.DistanceQuadTreeAlgorithm
import com.polestar.repository.data.charging.toLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import kotlin.math.abs

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
                    postCalcClusters()
                }
            }
        }
    }

    fun isCloseToPosition(
        source: LatLng,
        position: LatLng = LatLng(31.218953, 121.476741),
        delta: Float = 0.005f
    ): Boolean {
        val latClose = abs(source.latitude - position.latitude) <= delta
        val lngClose = abs(source.longitude - position.longitude) <= delta

        return latClose && lngClose
    }

    fun calcClusters(distanceInfo: DistanceInfo) {
        if (distanceInfo.sameZoom(this.distanceInfo)) {
            logd("sameZoom", "MarkerActionViewModel")
            return
        }
        //logd("calcClusters: ${distanceInfo.zoomLevel}", "MarkerActionViewModel")

        this.distanceInfo = distanceInfo
        postCalcClusters()
    }

    private fun postCalcClusters() {
        if (clusterAlgorithm.isFeed().not()) {
            logd("not feed", "MarkerActionViewModel")
            return
        }

        if (distanceInfo == null) {
            logd("not distanceInfo", "MarkerActionViewModel")
            return
        }

        distanceInfo?.let { distanceInfo ->
            viewModelScope.launch(Dispatchers.IO) {
                clusterAlgorithm.calc(distanceInfo) {
                    val curr = MarkerDataFactory.create(it)

                    prev?.let {

                        if (ClusterUtils.isNoSameHashCode(it).not()) {
                            throw IllegalArgumentException("eeeee")
                        }

                        val p = ClusterUtils.createClusterAnimData(it, curr, distanceInfo.zoomLevel)
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
        logd("prev size:${prev?.size}", "MarkerActionViewModel")
        prev?.forEach {
            logd("list marker:${it.getId()}", "MarkerActionViewModel")
        }
    }

    fun printPrevTotalStation() {
        logd("prev size:${prev?.size}", "MarkerActionViewModel")
        logd("prev station total:${prev?.sumOf { it.getSize() }}", "MarkerActionViewModel")
    }
}