package com.polestar.charging.ui.cluster.view;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.polestar.charging.ui.cluster.base.Cluster;
import com.polestar.charging.ui.cluster.base.ClusterItem;
import com.polestar.charging.ui.cluster.ui.IconGenerator;
import com.polestar.charging.ui.cluster.view.renderer.MarkerCache;
import com.polestar.charging.ui.cluster.view.renderer.MarkerManager;

import java.util.Set;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultClusterRenderer<T extends ClusterItem> {
    /**
     * Markers for single ClusterItems.
     */
    final MarkerCache<T> mMarkerCache = new MarkerCache<>();
    final MarkerCache<Cluster<T>> mClusterMarkerCache = new MarkerCache<>();


    /**
     * If cluster size is less than this size, display individual markers.
     */
    final static int MIN_CLUSTER_SIZE = 1;

    Set<? extends Cluster<T>> mClustersOld;

    private final ViewModifier mViewModifier;

    final MarkerManager mMarkerManager;
    final MarkerManager.Collection mClusterMarkers;
    final MarkerManager.Collection mSingleMarkers;
    final IconGenerator mIconGenerator;

    public DefaultClusterRenderer(Context context, AMap map) {
        mMarkerManager = new MarkerManager(map);
        mClusterMarkers = mMarkerManager.newCollection();
        mSingleMarkers = mMarkerManager.newCollection();
        mIconGenerator = new IconGenerator(context);
        mViewModifier = new ViewModifier(this);
    }

    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        mViewModifier.queue(clusters);
    }
}
