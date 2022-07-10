package com.example.amaptest.ui.main

import com.amap.api.maps.model.LatLng

object MockUtils {

    //白玉兰
    fun mockBaiYulan() = LatLng(DEFAULT_LAT, DEFAULT_LNG)


    //上海黄浦区领展企业广场
    fun mockLingZhan() = LatLng(31.22128, 121.476231)

    // 白玉兰
    const val DEFAULT_LNG = 121.497798
    const val DEFAULT_LAT = 31.249051

}