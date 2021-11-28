package com.polestar.charging.ui.cluster

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.animation.DecelerateInterpolator
import com.amap.api.maps.model.LatLng
import com.polestar.charging.ui.cluster.base.ClusterItem

class AnimationTask<T: ClusterItem>(
    val markerWithPosition: MarkerWithPosition,
    val from: LatLng,
    val to: LatLng,
    val zoomCallback: RenderTask.OnZoomChange<T>
) : AnimatorListenerAdapter(), AnimatorUpdateListener {
    var removeOnComplete = false

    fun perform() = ValueAnimator.ofFloat(0.0f, 1.0f).also {
        it.interpolator = ANIMATION_INTERP
        it.addUpdateListener(this)
        it.addListener(this)
        it.start()
    }

    override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
        if (removeOnComplete) {
            zoomCallback.removeMarker(markerWithPosition.marker)
        }
        markerWithPosition.position = to
    }

    fun removeOnAnimationComplete(){
        removeOnComplete = true
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        valueAnimator.animatedFraction.let {
            val lat = (to.latitude - from.latitude) * it + from.latitude
            var lngDelta = to.longitude - from.longitude
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360
            }
            val lng = lngDelta * it + from.longitude
            LatLng(lat, lng)
        }.let {
            markerWithPosition.marker.position = it
        }
    }

    companion object {
        val ANIMATION_INTERP: TimeInterpolator = DecelerateInterpolator()
    }
}