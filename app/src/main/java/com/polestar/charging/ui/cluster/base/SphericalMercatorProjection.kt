package com.polestar.charging.ui.cluster.base

import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.Point

class SphericalMercatorProjection(val mWorldWidth: Double) {
    fun toPoint(latLng: LatLng): Point {
        val x = latLng.longitude / 360 + .5
        val siny = Math.sin(Math.toRadians(latLng.latitude))
        val y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5
        return Point(x * mWorldWidth, y * mWorldWidth)
    }

    fun toLatLng(point: Point): LatLng {
        val x = point.x / mWorldWidth - 0.5
        val lng = x * 360
        val y = .5 - point.y / mWorldWidth
        val lat = 90 - Math.toDegrees(Math.atan(Math.exp(-y * 2 * Math.PI)) * 2)
        return LatLng(lat, lng)
    }
}