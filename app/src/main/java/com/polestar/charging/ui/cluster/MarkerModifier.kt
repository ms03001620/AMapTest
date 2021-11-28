package com.polestar.charging.ui.cluster

import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import java.util.*

class MarkerModifier<T: ClusterItem>(
    val mMarkerCache: MarkerCache<T>,
    val mClusterMarkerCache: MarkerCache<Cluster<T>>,
    val zoomCallback: RenderTask.OnZoomChange<T>
) {
    val mCreateMarkerTasks = LinkedList<CreateMarkerTask<T>>()
    val mOnScreenCreateMarkerTasks = LinkedList<CreateMarkerTask<T>>()
    val mRemoveMarkerTasks = LinkedList<Marker>()
    val mOnScreenRemoveMarkerTasks = LinkedList<Marker>()
    val mAnimationTasks = LinkedList<AnimationTask<T>>()

    fun add(priority: Boolean, c: CreateMarkerTask<T>) {
        if (priority) {
            mOnScreenCreateMarkerTasks.add(c)
        } else {
            mCreateMarkerTasks.add(c)
        }
    }

    fun performNextTask(){
        if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
            removeMarker(mOnScreenRemoveMarkerTasks.poll())
        } else if (!mAnimationTasks.isEmpty()) {
            mAnimationTasks.poll().perform()
        } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
            mOnScreenCreateMarkerTasks.poll().perform(this)
        } else if (!mCreateMarkerTasks.isEmpty()) {
            mCreateMarkerTasks.poll().perform(this)
        } else if (!mRemoveMarkerTasks.isEmpty()) {
            removeMarker(mRemoveMarkerTasks.poll())
        }
    }

    fun removeMarker(marker: Marker) {
        zoomCallback.removeMarker(marker)
    }

    fun animate(markerWithPosition: MarkerWithPosition,from: LatLng, to: LatLng) {
        mAnimationTasks.add(AnimationTask(markerWithPosition, from, to, zoomCallback))
    }

    fun animateThenRemove(marker: MarkerWithPosition, from: LatLng, to: LatLng){
        val task=  AnimationTask(marker, from, to, zoomCallback)
        task.removeOnAnimationComplete()
        mAnimationTasks.add(task)
    }

    fun remove(priority: Boolean, m: Marker){
        if(priority){
            mOnScreenRemoveMarkerTasks.add(m)
        }else{
            mRemoveMarkerTasks.add(m)
        }
    }

}