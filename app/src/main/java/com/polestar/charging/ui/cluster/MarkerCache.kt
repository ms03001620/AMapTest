package com.polestar.charging.ui.cluster

import com.amap.api.maps.model.Marker
import com.polestar.charging.ui.cluster.base.ClusterItem
import java.util.HashMap

class MarkerCache<T> {
    private val mCache = HashMap<T, Marker>()
    private val mCacheReverse = HashMap<Marker, T>()

    fun get(item: T): Marker? {
        return mCache.get(item)
    }


/*    private Map<T, Marker> mCache = new HashMap<>();
    private Map<Marker, T> mCacheReverse = new HashMap<>();

    public Marker get(T item) {
        return mCache.get(item);
    }*/



    fun get(item: Marker) = mCacheReverse.get(item)

    fun put(item: T, marker: Marker) {
        mCache[item] = marker
        mCacheReverse[marker] = item
    }

    fun remove(marker: Marker) {
        val t = mCacheReverse.get(marker)
        mCacheReverse.remove(marker)
        mCache.remove(t)
    }
}