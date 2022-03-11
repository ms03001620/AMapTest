package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng
import java.util.*

class StationClusterItem(val stationDetail: StationDetail) : ClusterItem {
    override val id: String
        get() = stationDetail.id ?: ""
    override val position: LatLng
        get() = stationDetail.toLatLng()
    override val title: String
        get() = stationDetail.stationid ?: "NoneId"
    override val snippet: String
        get() = stationDetail.providerName + stationDetail.stationName

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun equals(other: Any?): Boolean {
        if (other is StationClusterItem) {
            if (other.id == this.id) {
                return true
            }
        }
        return false
    }
}