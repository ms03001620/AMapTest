package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail
import com.quadtree.ClusterItem

class StationClusterItem(val stationDetail: StationDetail) : ClusterItem {
    override val position: LatLng
        get() = LatLng(stationDetail.lat ?: Double.NaN, stationDetail.lng ?: Double.NaN)
    override val title: String
        get() = stationDetail.stationid ?: "NoneId"
    override val snippet: String
        get() = stationDetail.providerName + stationDetail.stationName

}