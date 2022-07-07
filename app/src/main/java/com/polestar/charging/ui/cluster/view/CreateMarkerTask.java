package com.polestar.charging.ui.cluster.view;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.polestar.charging.ui.cluster.base.Cluster;
import com.polestar.charging.ui.cluster.base.ClusterItem;
import com.polestar.charging.ui.cluster.view.renderer.MarkerWithPosition;

import java.util.Set;

/**
 * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
 */
public class CreateMarkerTask<T extends ClusterItem> {
    private final Cluster<T> cluster;
    private final Set<MarkerWithPosition> newMarkers;
    private final LatLng animateFrom;
    DefaultClusterRenderer renderer;

    public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom,    DefaultClusterRenderer renderer) {
        this.cluster = c;
        this.newMarkers = markersAdded;
        this.animateFrom = animateFrom;
        this.renderer = renderer;
    }

    void perform(MarkerModifier markerModifier) {
        // Don't show small clusters. Render the markers inside, instead.
        if (!shouldRenderAsCluster(cluster)) {
            for (T item : cluster.getItems()) {
                Marker marker = renderer.mMarkerCache.get(item);
                MarkerWithPosition markerWithPosition;
                if (marker == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    if (animateFrom != null) {
                        markerOptions.position(animateFrom);
                    } else {
                        markerOptions.position(item.getPosition());
                    }
                    renderer.mMarkerManager.onBeforeClusterItemRendered(
                            item.getTitle(),
                            item.getSnippet(),
                            markerOptions
                    );
                    marker = renderer.mSingleMarkers.addMarker(markerOptions);
                    markerWithPosition = new MarkerWithPosition(marker);
                    renderer.mMarkerCache.put(item, marker);
                    if (animateFrom != null) {
                        markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
                    }
                } else {
                    markerWithPosition = new MarkerWithPosition(marker);
                    renderer.mMarkerManager.onClusterItemUpdated(item.getTitle(), item.getSnippet(), item.getPosition(), marker);
                }
                newMarkers.add(markerWithPosition);
            }
            return;
        }

        Marker marker = renderer.mClusterMarkerCache.get(cluster);
        MarkerWithPosition markerWithPosition;
        if (marker == null) {
            MarkerOptions markerOptions = new MarkerOptions().
                    position(animateFrom == null ? cluster.getPosition() : animateFrom);
            markerOptions.icon(renderer.mIconGenerator.getDescriptorForCluster(cluster.getSize()));
            marker = renderer.mClusterMarkers.addMarker(markerOptions);
            renderer.mClusterMarkerCache.put(cluster, marker);
            markerWithPosition = new MarkerWithPosition(marker);
            if (animateFrom != null) {
                markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
            }
        } else {
            markerWithPosition = new MarkerWithPosition(marker);
            marker.setIcon(renderer.mIconGenerator.getDescriptorForCluster(cluster.getSize()));
        }
        newMarkers.add(markerWithPosition);
    }

    boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() >= DefaultClusterRenderer.MIN_CLUSTER_SIZE;
    }
}