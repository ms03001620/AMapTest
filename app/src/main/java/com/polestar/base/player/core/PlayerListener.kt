package com.polestar.base.player.core

/**
 * Created by lwh 2021/9/1 10:49
 */
interface PlayerListener {

    fun onVideoSizeChange(width: Int, height: Int)

    fun onPlayerPrepare()

    fun onPlayerStateChange(isPlaying: Boolean)

    fun onPlayComplete()

    fun onRenderFirstFrame()

    fun onVideoFrameRendering(presentationTimeUs: Long)

    fun onPlayEncounterError(ex: Exception?)
}
