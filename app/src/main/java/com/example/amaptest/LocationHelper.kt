package com.example.amaptest

import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationListener

class LocationHelper(
    private val impl: OnLocation,
    private val callback: OnEvent
) {
    interface OnEvent {
        fun onLoading()
        fun onResult(aMapLocation: AMapLocation)
        fun onError(aMapLocation: AMapLocation)
    }

    interface OnLocation {
        fun start(listener: AMapLocationListener)
        fun stop()
    }

    fun onStart() {
        callback.onLoading()
        impl.start(listener)
    }

    fun onStop() {
        impl.stop()
    }

    private val listener = AMapLocationListener {
        it?.let {
            //error code, https://lbs.amap.com/api/android-location-sdk/guide/utilities/errorcode/
            if ((it.errorCode) == 0) {
                callback.onResult(it)
                impl.stop()
            } else {
                callback.onError(it)
            }
        }
    }
}