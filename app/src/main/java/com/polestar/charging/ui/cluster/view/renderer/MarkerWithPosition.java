package com.polestar.charging.ui.cluster.view.renderer;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

/**
 * A Marker and its position. {@link Marker#getPosition()} must be called from the UI thread, so this
 * object allows lookup from other threads.
 */
public class MarkerWithPosition {
    private final Marker marker;
    private LatLng position;

    public MarkerWithPosition(Marker marker) {
        this.marker = marker;
        position = marker.getPosition();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MarkerWithPosition) {
            return marker.equals(((MarkerWithPosition) other).marker);
        }
        return false;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public LatLng getPosition() {
        return position;
    }

    public Marker getMarker() {
        return marker;
    }

    @Override
    public int hashCode() {
        return marker.hashCode();
    }
}