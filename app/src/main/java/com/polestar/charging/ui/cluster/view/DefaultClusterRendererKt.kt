package com.polestar.charging.ui.cluster.view

import android.content.Context
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.amap.api.maps.AMap
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.StaticCluster
import com.polestar.charging.ui.cluster.ui.IconGenerator
import com.polestar.charging.ui.cluster.view.renderer.MarkerCache
import com.polestar.charging.ui.cluster.view.renderer.MarkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
class DefaultClusterRendererKt<T : ClusterItem>(
    context: Context?, map: AMap?, val coroutineScope: CoroutineScope
) {
    lateinit var mMarkerCache: MarkerCache<T>
    lateinit var mClustersOld: MutableSet<out Cluster<T>>
    lateinit var mMarkerManager: MarkerManager


    lateinit var mClusterMarkerCache : MarkerCache<Cluster<T>>

    //private final ViewModifier mViewModifier;

    val viewModifierKotlin: ViewModifierKotlin
    lateinit var   mClusterMarkers: MarkerManager.Collection
    lateinit var  mSingleMarkers: MarkerManager.Collection
    lateinit var   mIconGenerator: IconGenerator

    fun onClustersChanged(clusters: Set<Cluster<T>>) {
/*        viewModifierKotlin.appendTaskToTail(object : ViewModifierKotlin.WorkTask {
            override suspend fun work(function: () -> Unit) {

                coroutineScope.launch(Dispatchers.Main) {

                    val t = RenderTask(clusters, this@DefaultClusterRendererKt)
                    t.setCallback {
                        function.invoke()
                    }
                    t.run()
                }

            }
        })*/
    }

    companion object {
        /**
         * If cluster size is less than this size, display individual markers.
         */
        const val MIN_CLUSTER_SIZE = 1
    }

    init {
        mMarkerManager = MarkerManager(map)
        mClusterMarkers = mMarkerManager.newCollection()
        mSingleMarkers = mMarkerManager.newCollection()
        mIconGenerator = IconGenerator(context)
        viewModifierKotlin = ViewModifierKotlin(coroutineScope)
    }
}