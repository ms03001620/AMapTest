package com.polestar.charging.ui.cluster.view;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.polestar.charging.ui.cluster.base.Cluster;
import com.polestar.charging.ui.cluster.base.ClusterItem;
import com.polestar.charging.ui.cluster.base.Point;
import com.polestar.charging.ui.cluster.base.SphericalMercatorProjection;
import com.polestar.charging.ui.cluster.view.renderer.MarkerWithPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
 * new zoom level and set of clusters.
 * <p/>
 * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
 * be executed by a MarkerModifier.
 * <p/>
 * There are three stages for the render:
 * <p/>
 * 1. Markers are added to the map
 * <p/>
 * 2. Markers are animated to their final position
 * <p/>
 * 3. Any old markers are removed from the map
 * <p/>
 * When zooming in, markers are animated out from the nearest existing cluster. When zooming
 * out, existing clusters are animated to the nearest new cluster.
 */
class RenderTask<T extends ClusterItem> implements Runnable {

    /**
     * Markers that are currently on the map.
     */
    private static Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap());


    /**
     * The target zoom level for the current set of clusters.
     */
    private static float mZoom;


    final Set<? extends Cluster<T>> clusters;

    DefaultClusterRenderer renderer;


    private Runnable mCallback;
    private Projection mProjection;
    private SphericalMercatorProjection mSphericalMercatorProjection;
    private float mMapZoom;

    public RenderTask(Set<? extends Cluster<T>> clusters, DefaultClusterRenderer renderer) {
        this.clusters = clusters;
        this.renderer = renderer;
    }

    /**
     * A callback to be run when all work has been completed.
     */
    public void setCallback(Runnable callback) {
        mCallback = callback;
    }

    public void setProjection(Projection projection) {
        this.mProjection = projection;
    }

    public void setMapZoom(float zoom) {
        this.mMapZoom = zoom;
        this.mSphericalMercatorProjection = new SphericalMercatorProjection(256 * Math.pow(2, Math.min(zoom, mZoom)));
    }

    @SuppressLint("NewApi")
    public void run() {
        if (!shouldRender(immutableOf(renderer.mClustersOld), immutableOf(clusters))) {
            if(mCallback!=null){
                mCallback.run();
            }
            return;
        }

        setProjection(renderer.mMarkerManager.getProjection());
        setMapZoom(renderer.mMarkerManager.getCameraPositionZoom());

        // Prevent crashes: https://issuetracker.google.com/issues/35827242
        LatLngBounds visibleBounds;
        try {
            visibleBounds = mProjection.getVisibleRegion().latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            visibleBounds = LatLngBounds.builder()
                    .include(new LatLng(0, 0))
                    .build();
        }
        // TODO: Add some padding, so that markers can animate in from off-screen.

        // Find all of the existing clusters that are on-screen. These are candidates for
        // markers to animate from.
        List<Point> existingClustersOnScreen = null;
        if (renderer.mClustersOld != null) {
            final Set<? extends Cluster<T>> clusters1 = renderer.mClustersOld;

            existingClustersOnScreen = new ArrayList<>();
            for (Cluster<T> c : clusters1) {
                if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                    Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                    existingClustersOnScreen.add(point);
                }
            }
        }

        final MarkerModifier markerModifier = new MarkerModifier(new MarkerModifier.OnRemove() {
            @Override
            public void onRemove(Marker m) {
                renderer.mMarkerCache.remove(m);
                renderer.mClusterMarkerCache.remove(m);
                renderer.mMarkerManager.remove(m);
            }
        });

        // Create the new markers and animate them to their new positions.
        final Set<MarkerWithPosition> newMarkers = Collections.newSetFromMap(
                new ConcurrentHashMap<MarkerWithPosition, Boolean>());
        for (Cluster<T> c : clusters) {
            boolean onScreen = visibleBounds.contains(c.getPosition());
            if (zoomingIn() && onScreen) {
                Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                Point closest = findClosestCluster(existingClustersOnScreen, point);
                if (closest != null) {
                    LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                    markerModifier.add(true, new CreateMarkerTask(c, newMarkers, animateTo, renderer));
                } else {
                    markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null, renderer));
                }
            } else {
                markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null, renderer));
            }
        }

        // Wait for all markers to be added.
        markerModifier.waitUntilFree();

        // Don't remove any markers that were just added. This is basically anything that had
        // a hit in the MarkerCache.
        final Set<MarkerWithPosition> markersToRemove = mMarkers;
        markersToRemove.removeAll(newMarkers);

        // Find all of the new clusters that were added on-screen. These are candidates for
        // markers to animate from.
        List<Point> newClustersOnScreen = new ArrayList<>();
        for (Cluster<T> c : clusters) {
            if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                newClustersOnScreen.add(p);
            }
        }
        // Remove the old markers, animating them into clusters if zooming out.
        for (final MarkerWithPosition marker : markersToRemove) {
            boolean onScreen = visibleBounds.contains(marker.getPosition());
            // Don't animate when zooming out more than 3 zoom levels.
            // TODO: drop animation based on speed of device & number of markers to animate.
            if (!zoomingIn() && zoomDelta() > -3 && onScreen) {
                final Point point = mSphericalMercatorProjection.toPoint(marker.getPosition());
                final Point closest = findClosestCluster(newClustersOnScreen, point);
                if (closest != null) {
                    LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                    markerModifier.animateThenRemove(marker, marker.getPosition(), animateTo);
                } else {
                    markerModifier.remove(true, marker.getMarker());
                }
            } else {
                markerModifier.remove(onScreen, marker.getMarker());
            }
        }

        markerModifier.waitUntilFree();

        mMarkers = newMarkers;
        renderer.mClustersOld = clusters;
        mZoom = mMapZoom;
        if(mCallback!=null){
            mCallback.run();
        }
    }

    boolean zoomingIn() {
        return mMapZoom > mZoom;
    }

    float zoomDelta() {
        return mMapZoom - mZoom;
    }

    /**
     * Determines if the new clusters should be rendered on the map, given the old clusters. This
     * method is primarily for optimization of performance, and the default implementation simply
     * checks if the new clusters are equal to the old clusters, and if so, it returns false.
     * <p>
     * However, there are cases where you may want to re-render the clusters even if they didn't
     * change. For example, if you want a cluster with one item to render as a cluster above
     * a certain zoom level and as a marker below a certain zoom level (even if the contents of the
     * clusters themselves did not change). In this case, you could check the zoom level in an
     * implementation of this method and if that zoom level threshold is crossed return true, else
     * {@code return super.shouldRender(oldClusters, newClusters)}.
     * <p>
     * Note that always returning true from this method could potentially have negative performance
     * implications as clusters will be re-rendered on each pass even if they don't change.
     *
     * @param oldClusters The clusters from the previous iteration of the clustering algorithm
     * @param newClusters The clusters from the current iteration of the clustering algorithm
     * @return true if the new clusters should be rendered on the map, and false if they should not. This
     * method is primarily for optimization of performance, and the default implementation simply
     * checks if the new clusters are equal to the old clusters, and if so, it returns false.
     */
    boolean shouldRender(@NonNull Set<? extends Cluster<T>> oldClusters, @NonNull Set<? extends Cluster<T>> newClusters) {
        return !newClusters.equals(oldClusters);
    }

    Set<? extends Cluster<T>> immutableOf(Set<? extends Cluster<T>> clusters) {
        return clusters != null ? Collections.unmodifiableSet(clusters) : Collections.emptySet();
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     *
     * @param cluster cluster to examine for rendering
     * @return true if the provided cluster should be rendered as a single marker on the map, false
     * if the items within this cluster should be rendered as individual markers instead.
     */
    boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() >= DefaultClusterRenderer.MIN_CLUSTER_SIZE;
    }

    Point findClosestCluster(List<Point> markers, Point point) {
        if (markers == null || markers.isEmpty()) return null;

        int maxDistance = 100;/*NonHierarchicalDistanceBasedAlgorithm.DEFAULT_MAX_DISTANCE_AT_ZOOM;*/
        double minDistSquared = maxDistance * maxDistance;
        Point closest = null;
        for (Point candidate : markers) {
            double dist = distanceSquared(candidate, point);
            if (dist < minDistSquared) {
                closest = candidate;
                minDistSquared = dist;
            }
        }
        return closest;
    }

    double distanceSquared(Point a, Point b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }
}
