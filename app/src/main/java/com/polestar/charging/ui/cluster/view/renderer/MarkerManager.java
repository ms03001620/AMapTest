package com.polestar.charging.ui.cluster.view.renderer;

import androidx.annotation.NonNull;


import com.amap.api.maps.AMap;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
public class MarkerManager {
    protected final Map<Marker, Collection> mAllObjects = new HashMap<>();

    AMap mMap;

    public MarkerManager(AMap map) {
        this.mMap = map;
    }

    public Collection newCollection() {
        return new Collection();
    }

    public boolean remove(Marker object) {
        Collection collection = mAllObjects.get(object);
        return collection != null && collection.remove(object);
    }

    public Projection getProjection() {
        return mMap.getProjection();
    }

    public float getCameraPositionZoom() {
        return mMap.getCameraPosition().zoom;
    }


    public void onBeforeClusterItemRendered(String title,
                                            String snippet,
                                            MarkerOptions markerOptions) {
        if (title != null && snippet != null) {
            markerOptions.title(title);
            markerOptions.snippet(snippet);
        } else if (title != null) {
            markerOptions.title(title);
        } else if (snippet != null) {
            markerOptions.title(snippet);
        }
    }

    public void onClusterItemUpdated(
            String title,
            String snippet,
            LatLng position,
            @NonNull Marker marker) {
        boolean changed = false;
        // Update marker text if the item text changed - same logic as adding marker in CreateMarkerTask.perform()
        if (title != null && snippet != null) {
            if (!title.equals(marker.getTitle())) {
                marker.setTitle(title);
                changed = true;
            }
            if (!snippet.equals(marker.getSnippet())) {
                marker.setSnippet(snippet);
                changed = true;
            }
        } else if (snippet != null && !snippet.equals(marker.getTitle())) {
            marker.setTitle(snippet);
            changed = true;
        } else if (title != null && !title.equals(marker.getTitle())) {
            marker.setTitle(title);
            changed = true;
        }
        // Update marker position if the item changed position
        if (!marker.getPosition().equals(position)) {
            marker.setPosition(position);
            changed = true;
        }
        if (changed && marker.isInfoWindowShown()) {
            // Force a refresh of marker info window contents
            marker.showInfoWindow();
        }
    }

    public class Collection {
        private final Set<Marker> mObjects = new LinkedHashSet<>();

        public Collection() {
        }

        protected boolean remove(Marker object) {
            if (mObjects.remove(object)) {
                mAllObjects.remove(object);
                object.remove();
                return true;
            }
            return false;
        }

        public Marker addMarker(MarkerOptions opts) {
            Marker marker = mMap.addMarker(opts);
            mObjects.add(marker);
            mAllObjects.put(marker, this);
            return marker;
        }
    }

}
