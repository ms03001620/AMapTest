package com.polestar.charging.ui.cluster.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import androidx.annotation.NonNull;


import com.amap.api.maps.AMap;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.polestar.charging.ui.cluster.base.Cluster;
import com.polestar.charging.ui.cluster.base.ClusterItem;
import com.polestar.charging.ui.cluster.base.Point;
import com.polestar.charging.ui.cluster.base.SphericalMercatorProjection;
import com.polestar.charging.ui.cluster.ui.IconGenerator;
import com.polestar.charging.ui.cluster.view.renderer.AnimationTask;
import com.polestar.charging.ui.cluster.view.renderer.AnimationTaskCallback;
import com.polestar.charging.ui.cluster.view.renderer.MarkerCache;
import com.polestar.charging.ui.cluster.view.renderer.MarkerManager;
import com.polestar.charging.ui.cluster.view.renderer.MarkerWithPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultClusterRenderer<T extends ClusterItem> {
    private final IconGenerator mIconGenerator;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    /**
     * Markers that are currently on the map.
     */
    private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Markers for single ClusterItems.
     */
    private MarkerCache<T> mMarkerCache = new MarkerCache<>();
    private MarkerCache<Cluster<T>> mClusterMarkerCache = new MarkerCache<>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private int mMinClusterSize = 4;

    /**
     * The currently displayed set of clusters.
     */
    private Set<? extends Cluster<T>> mClusters;

    /**
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final ViewModifier mViewModifier = new ViewModifier();

    MarkerManager mMarkerManager;

    private final MarkerManager.Collection mSigleMarkers;
    private final MarkerManager.Collection mClusterMarkers;

    public DefaultClusterRenderer(Context context, AMap map) {
        mMarkerManager = new MarkerManager(map);
        mClusterMarkers = mMarkerManager.newCollection();
        mSigleMarkers = mMarkerManager.newCollection();
        mIconGenerator = new IconGenerator(context);
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    @SuppressLint("HandlerLeak")
    private class ViewModifier extends Handler {
        private static final int RUN_TASK = 0;
        private static final int TASK_FINISHED = 1;
        private boolean mViewModificationInProgress = false;
        private RenderTask mNextClusters = null;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false;
                if (mNextClusters != null) {
                    // Run the task that was queued up.
                    sendEmptyMessage(RUN_TASK);
                }
                return;
            }
            removeMessages(RUN_TASK);

            if (mViewModificationInProgress) {
                // Busy - wait for the callback.
                return;
            }

            if (mNextClusters == null) {
                // Nothing to do.
                return;
            }

            Projection projection = mMarkerManager.getProjection();

            RenderTask renderTask;
            synchronized (this) {
                renderTask = mNextClusters;
                mNextClusters = null;
                mViewModificationInProgress = true;
            }

            renderTask.setCallback(new Runnable() {
                @Override
                public void run() {
                    sendEmptyMessage(TASK_FINISHED);
                }
            });
            renderTask.setProjection(projection);
            renderTask.setMapZoom(mMarkerManager.getCameraPositionZoom());
            mExecutor.execute(renderTask);
        }

        public void queue(Set<? extends Cluster<T>> clusters) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new RenderTask(clusters);
            }
            sendEmptyMessage(RUN_TASK);
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     *
     * @param cluster cluster to examine for rendering
     * @return true if the provided cluster should be rendered as a single marker on the map, false
     * if the items within this cluster should be rendered as individual markers instead.
     */
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() >= mMinClusterSize;
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
    protected boolean shouldRender(@NonNull Set<? extends Cluster<T>> oldClusters, @NonNull Set<? extends Cluster<T>> newClusters) {
        return !newClusters.equals(oldClusters);
    }

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
    private class RenderTask implements Runnable {
        final Set<? extends Cluster<T>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;

        private RenderTask(Set<? extends Cluster<T>> clusters) {
            this.clusters = clusters;
        }

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
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
            if (!shouldRender(immutableOf(DefaultClusterRenderer.this.mClusters), immutableOf(clusters))) {
                mCallback.run();
                return;
            }
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
            if (DefaultClusterRenderer.this.mClusters != null) {
                existingClustersOnScreen = new ArrayList<>();
                for (Cluster<T> c : DefaultClusterRenderer.this.mClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
            }

            final MarkerModifier markerModifier = new MarkerModifier();

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
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, animateTo));
                    } else {
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null));
                    }
                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null));
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
            DefaultClusterRenderer.this.mClusters = clusters;
            mZoom = mMapZoom;
            mCallback.run();
        }

        boolean zoomingIn() {
            return mMapZoom > mZoom;
        }

        float zoomDelta() {
            return mMapZoom - mZoom;
        }
    }

    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        mViewModifier.queue(clusters);
    }

    private Set<? extends Cluster<T>> immutableOf(Set<? extends Cluster<T>> clusters) {
        return clusters != null ? Collections.unmodifiableSet(clusters) : Collections.emptySet();
    }

    private static double distanceSquared(Point a, Point b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    private Point findClosestCluster(List<Point> markers, Point point) {
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

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
     * animating a markerWithPosition) is performed while trying not to block the rest of the app's
     * UI.
     */
    @SuppressLint("HandlerLeak")
    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<>();
        private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<>();
        private Queue<AnimationTask> mAnimationTasks = new LinkedList<>();

        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private boolean mListenerAdded;

        private MarkerModifier() {
            super(Looper.getMainLooper());
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        public void add(boolean priority, CreateMarkerTask c) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c);
            } else {
                mCreateMarkerTasks.add(c);
            }
            lock.unlock();
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        public void remove(boolean priority, Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m);
            } else {
                mRemoveMarkerTasks.add(m);
            }
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        public void animate(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            mAnimationTasks.add(new AnimationTask(marker, from, to, null));
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            AnimationTask animationTask = new AnimationTask(marker, from, to, new AnimationTaskCallback() {
                public void onAnimationEnd(Marker marker) {
                    mMarkerCache.remove(marker);
                    mClusterMarkerCache.remove(marker);
                    mMarkerManager.remove(marker);
                }
            });
            mAnimationTasks.add(animationTask);
            lock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this);
                mListenerAdded = true;
            }
            removeMessages(BLANK);

            lock.lock();
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (int i = 0; i < 10; i++) {
                    performNextTask();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll();
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10);
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll());
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll().perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll().perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll().perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll());
            }
        }

        private void removeMarker(Marker m) {
            mMarkerCache.remove(m);
            mClusterMarkerCache.remove(m);
            mMarkerManager.remove(m);
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                        mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                        mAnimationTasks.isEmpty()
                );
            } finally {
                lock.unlock();
            }
        }

        /**
         * Blocks the calling thread until all work has been processed.
         */
        public void waitUntilFree() {
            while (isBusy()) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK);
                lock.lock();
                try {
                    if (isBusy()) {
                        busyCondition.await();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean queueIdle() {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK);
            return true;
        }
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private class CreateMarkerTask {
        private final Cluster<T> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private final LatLng animateFrom;

        public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
        }

        private void perform(MarkerModifier markerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                for (T item : cluster.getItems()) {
                    Marker marker = mMarkerCache.get(item);
                    MarkerWithPosition markerWithPosition;
                    if (marker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (animateFrom != null) {
                            markerOptions.position(animateFrom);
                        } else {
                            markerOptions.position(item.getPosition());
                        }
                        mMarkerManager.onBeforeClusterItemRendered(
                                item.getTitle(),
                                item.getSnippet(),
                                markerOptions
                        );
                        marker = mSigleMarkers.addMarker(markerOptions);
                        markerWithPosition = new MarkerWithPosition(marker);
                        mMarkerCache.put(item, marker);
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
                        }
                    } else {
                        markerWithPosition = new MarkerWithPosition(marker);
                        mMarkerManager.onClusterItemUpdated(item.getTitle(), item.getSnippet(), item.getPosition(), marker);
                    }
                    newMarkers.add(markerWithPosition);
                }
                return;
            }

            Marker marker = mClusterMarkerCache.get(cluster);
            MarkerWithPosition markerWithPosition;
            if (marker == null) {
                MarkerOptions markerOptions = new MarkerOptions().
                        position(animateFrom == null ? cluster.getPosition() : animateFrom);
                markerOptions.icon(mIconGenerator.getDescriptorForCluster(cluster.getSize()));
                marker = mClusterMarkers.addMarker(markerOptions);
                mClusterMarkerCache.put(cluster, marker);
                markerWithPosition = new MarkerWithPosition(marker);
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
                }
            } else {
                markerWithPosition = new MarkerWithPosition(marker);
                marker.setIcon(mIconGenerator.getDescriptorForCluster(cluster.getSize()));
            }
            newMarkers.add(markerWithPosition);
        }
    }
}
