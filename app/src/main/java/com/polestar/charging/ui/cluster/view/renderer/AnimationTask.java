package com.polestar.charging.ui.cluster.view.renderer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.animation.DecelerateInterpolator;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
    private static final TimeInterpolator ANIMATION_INTERP = new DecelerateInterpolator();

    private final MarkerWithPosition markerWithPosition;
    private final Marker marker;
    private final LatLng from;
    private final LatLng to;
    private AnimationTaskCallback callback;

    public AnimationTask(MarkerWithPosition markerWithPosition,
                          LatLng from,
                          LatLng to,
                          AnimationTaskCallback callback
    ) {
        this.markerWithPosition = markerWithPosition;
        this.marker = markerWithPosition.getMarker();
        this.from = from;
        this.to = to;
        this.callback = callback;
    }

    public void perform() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setInterpolator(ANIMATION_INTERP);
        valueAnimator.addUpdateListener(this);
        valueAnimator.addListener(this);
        valueAnimator.start();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (callback != null) {
            callback.onAnimationEnd(marker);
        }
        markerWithPosition.setPosition(to);
    }


    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float fraction = valueAnimator.getAnimatedFraction();
        double lat = (to.latitude - from.latitude) * fraction + from.latitude;
        double lngDelta = to.longitude - from.longitude;

        // Take the shortest path across the 180th meridian.
        if (Math.abs(lngDelta) > 180) {
            lngDelta -= Math.signum(lngDelta) * 360;
        }
        double lng = lngDelta * fraction + from.longitude;
        LatLng position = new LatLng(lat, lng);
        marker.setPosition(position);
    }
}