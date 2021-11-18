package com.example.amaptest

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import java.lang.ref.WeakReference

class LocationImpl(private val context: Context) : LocationHelper.OnLocation {
    private var client: WeakReference<AMapLocationClient>? = null
    override fun start(listener: AMapLocationListener) {
        client = WeakReference(AMapLocationClient(context))
        client?.get()?.stopLocation()
        client?.get()?.setLocationListener(listener)
        client?.get()?.setLocationOption(AMapLocationClientOption().apply {
            this.isOnceLocation = true;
            this.isOnceLocationLatest = true
        })
        client?.get()?.startLocation()
    }

    override fun stop() {
        client?.get()?.stopLocation()
        client?.clear()
    }
}