package com.quadtree;

import com.amap.api.maps.model.LatLng;

import java.util.Collection;


public interface Cluster<T extends ClusterItem> {
    LatLng getPosition();

    Collection<T> getItems();

    int getSize();
}
