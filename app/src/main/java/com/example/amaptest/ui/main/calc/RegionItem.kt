package com.example.amaptest.ui.main.calc

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail

class RegionItem(val stationDetail: StationDetail) : ClusterItem<StationDetail> {
    val latLng = LatLng(
        stationDetail.lat ?: 0.0,
        stationDetail.lng ?: 0.0
    )

    override fun getPosition() = latLng
    override fun getEntry(): StationDetail {
        return stationDetail
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.hashCode() == other.hashCode()) return true
        return false
    }

    override fun hashCode(): Int {
        return stationDetail.stationid.hashCode()
    }
}