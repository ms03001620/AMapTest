package com.polestar.base.player.core

import java.lang.ref.WeakReference

/**
 * lwh created in 2023/4/14 17:34
 */
class WeakPlayerListener(val reference: WeakReference<PlayerListener>) : PlayerListener {

    override fun onVideoSizeChange(width: Int, height: Int) {
        reference.get()?.onVideoSizeChange(width, height)
    }

    override fun onPlayerPrepare() {
        reference.get()?.onPlayerPrepare()
    }

    override fun onPlayerStateChange(isPlaying: Boolean) {
        reference.get()?.onPlayerStateChange(isPlaying)
    }

    override fun onPlayComplete() {
        reference.get()?.onPlayComplete()
    }

    override fun onRenderFirstFrame() {
        reference.get()?.onRenderFirstFrame()
    }

    override fun onVideoFrameRendering(presentationTimeUs: Long) {
        reference.get()?.onVideoFrameRendering(presentationTimeUs)
    }

    override fun onPlayEncounterError(ex: Exception?) {
        reference.get()?.onPlayEncounterError(ex)
    }
}
