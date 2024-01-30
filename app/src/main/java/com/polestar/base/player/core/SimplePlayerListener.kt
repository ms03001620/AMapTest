package com.polestar.base.player.core

import com.polestar.base.player.utils.PlayerThread

/**
 * Created by lwh 2021/9/1 14:31
 */
abstract class SimplePlayerListener : PlayerListener {
    override fun onVideoSizeChange(width: Int, height: Int) {
    }

    override fun onPlayerPrepare() {
    }

    override fun onPlayerStateChange(isPlaying: Boolean) {
    }

    override fun onPlayComplete() {
    }

    override fun onRenderFirstFrame() {
    }

    @PlayerThread
    override fun onVideoFrameRendering(presentationTimeUs: Long) {
    }

    override fun onPlayEncounterError(ex: Exception?) {
    }
}
