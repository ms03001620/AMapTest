package com.example.amaptest.marker

import com.amap.api.maps.model.LatLng

data class AnimTaskData(
    val addList: MutableList<BaseMarkerData>,
    val deleteList: MutableList<BaseMarkerData>,
    val cospList: HashMap<LatLng, MutableList<BaseMarkerData>>,
    val expList: HashMap<LatLng, MutableList<BaseMarkerData>>,
)