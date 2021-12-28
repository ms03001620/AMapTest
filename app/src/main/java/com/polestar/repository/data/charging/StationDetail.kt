package com.polestar.repository.data.charging

import androidx.annotation.VisibleForTesting
import com.amap.api.maps.model.LatLng
import java.io.Serializable
import java.lang.Exception
import java.util.*
import java.util.concurrent.LinkedBlockingDeque


/*"id": "9e297db2-d7da-425b-b68c-84ef42366e45",
"stationid": "OPEN_XXCD-33222355",
"providerNo": "OPEN_XXCD",
"providerName": "星星充电",
"stationNo": "33222355",

"stationName": "上海北外滩白玉兰广场充电站",
"lng": 121.498541,
"lat": 31.248555,
"acTotal": 21,
"dcTotal": 2,

"address": "上海市市辖区虹口区东大名路501号121",
"regionCode": "310000",
"imgUrl": "",
"serviceTime": "周一至周日00:00-24:00",
"stubGroupType": 0,

"stubGroupStatus": 1,
"parkingFeeInfo": "12元/小时，6元/半小时，封顶96元（仅供参考，以场地实际费用为准）",
"electricFee": [],
"supportCarType": 0,
"parkingFeeType": 0,

"distance": "0.08961417403",
"freeAcTotal": 0,
"freeDcTotal": 0,
favorites: 0

*/

const val VALUE_VALID = "1"
const val VALUE_INVALID = "0"
const val VALUE_DC = "1"
const val VALUE_AC = "0"

data class StationDetail(
    val id: String? = null,
    val stationid: String? = null,
    val providerNo: String? = null,
    val providerName: String? = null,
    val stationNo: String? = null,

    val stationName: String? = null,
    val lng: Double? = null,
    val lat: Double? = null,
    val acTotal: Int? = null,
    val dcTotal: Int? = null,

    val address: String? = null,
    val regionCode: String? = null,
    val imgUrl: String? = null,
    val serviceTime: String? = null,
    val stubGroupType: Int? = null,

    val stubGroupStatus: Int? = null,
    val parkingFeeInfo: String? = null,
    val electricFee: List<ElectricFee>? = null,
    val supportCarType: Int? = null,
    val parkingFeeType: Int? = null,

    val distance: Double? = null,
    val freeAcTotal: Int? = null,
    val freeDcTotal: Int? = null,
    val tagList: String? = null,
    val price: Double? = null,
    val favorites: String? = null,
    val gunKw: List<GunKw>? = null,
    val imgList: List<String?>? = null,
    val status: String? = null
) : Serializable

fun StationDetail.isFavorite() = favorites == VALUE_VALID

fun StationDetail.isInvalid() = status != VALUE_VALID   // 1 valid else invalid

fun StationDetail.isValid() = status == VALUE_VALID

fun StationDetail.freeAcDcAll() = (freeAcTotal ?: 0) + (freeDcTotal ?: 0)

fun StationDetail.showMarker() = (acTotal ?: 0 + dcTotal!!).toString()

fun StationDetail.getDetailImages(): MutableList<StationImage> {
    val data = mutableListOf<StationImage>()
    imgList?.filterNot {
        it.isNullOrEmpty()
    }?.forEach {
        it?.let {
            data.add(StationImage(it, false))
        }
    }
    if (data.isEmpty()) {
        // if no any images, add default image to list
        data.add(StationImage("", true))
    }
    return data
}

fun StationDetail.getAcKw() = gunKw?.firstOrNull {
    it.type == VALUE_AC
}?.kw

fun StationDetail.getDcKw() = gunKw?.firstOrNull() {
    it.type == VALUE_DC
}?.kw

fun StationDetail.toLatLng() = LatLng(lat?: Double.NaN, lng?: Double.NaN)

fun StationDetail.displayName(): String {
    var result = ""
    providerName?.let {
        result += it
    }
    stationName?.let {
        result += it
    }
    return result
}

fun StationDetail.formatDistance(): String? {
    if (distance == null) {
        return null
    }
    return distance.let {
        it / 1000.0
    }.let {
        if (it < .1) {
            0.1
        } else {
            it
        }
    }.let {
        String.format("%.1fkm", it)
    }
}

fun StationDetail.priceYuan() = price?.let {
    price / 100
}

fun StationDetail.decodeTags(): List<String>? {
    if (tagList.isNullOrBlank()) {
        return null
    }
    val result = tagList.split("|").filter {
        it.isNotBlank()
    }
    if (result.isNotEmpty()) {
        return result
    }
    return null
}

fun StationDetail.setFavorite(isFavorite: Boolean) = copy(
    favorites = if (isFavorite) VALUE_VALID else VALUE_INVALID
)

data class StationResp(
    val data: List<StationDetail>?,
    val pageInfo: PathData
)

class PathData(
    private val step: Int,
    var page: Int = PAGE_FIRST_INDEX,
    var data: ArrayList<StationDetail>?
) {
    private var lastSize = -1

    init {
        data?.let {
            updateSize(it.size)
        }
    }

    fun isFirstPage() = page == PAGE_FIRST_INDEX

    fun isEof() = lastSize != -1 && lastSize < step

    @VisibleForTesting
    fun updateSize(newSize: Int) {
        lastSize = newSize
    }

    fun append(newList: ArrayList<StationDetail>?) {
        newList?.let {
            updateSize(newList.size)
            data?.let {
                it.addAll(newList)
                page++
            }
        } ?: run {
            updateSize(0)
            page++
        }
    }

    fun updateFavorite(stationId: String, isFavorite: Boolean) {
        data?.filterIndexed { index, stationDetail ->
            stationDetail.stationid == stationId
        }?.forEach { stationDetail ->
            val index = data?.indexOf(stationDetail)
            index?.let {
                data?.add(
                    index, stationDetail.setFavorite(isFavorite)
                )
                data?.remove(stationDetail)
            }
        }
    }

    companion object {
        const val PAGE_FIRST_INDEX = 0
    }
}
