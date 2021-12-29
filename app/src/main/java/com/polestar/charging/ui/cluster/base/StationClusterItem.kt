package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail

class StationClusterItem(val stationDetail: StationDetail) : ClusterItem {
    override val id: String
        get() = stationDetail.id ?: ""
    override val position: LatLng
        get() = LatLng(stationDetail.lat ?: Double.NaN, stationDetail.lng ?: Double.NaN)
    override val title: String
        get() = stationDetail.stationid ?: "NoneId"
    override val snippet: String
        get() = stationDetail.providerName + stationDetail.stationName

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode() ?: 0
    }
}