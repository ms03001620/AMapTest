package com.polestar.base.player.impl

import android.content.Context
import android.net.Uri
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.video.VideoSize
import com.polestar.base.player.core.BasePlayerCore
import com.polestar.base.player.core.IPlayerCore
import com.polestar.base.player.core.MediaSourceAdapter
import kotlin.math.abs

/**
 * Created by lwh 2021/9/1 11:28
 * ExoPlayer包装实现
 */
class ExoPlayerCore(context: Context) : BasePlayerCore<SimpleExoPlayer>(context) {
    private val exoPlayer: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
    private var targetSeekPosition = -1L
    private var lastSeekRequestTime = 0L
    private var lastSeekCompleteTime = 0L
    private var handleSeekComplete = true
    private var seekOptimizeEnable = false
    private var exoPlayerRelease = false

    init {
        setupPlayer()
    }

    private fun setupPlayer() {
        exoPlayer.setVideoFrameMetadataListener { presentationTimeUs, _, _, _ ->
            for (listener in listeners) {
                listener.onVideoFrameRendering(presentationTimeUs)
            }
        }
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                for (listener in listeners) {
                    listener.onPlayerStateChange(isPlaying)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                for (listener in listeners) {
                    listener.onPlayEncounterError(error)
                }
            }

            override fun onRenderedFirstFrame() {
                for (listener in listeners) {
                    listener.onRenderFirstFrame()
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                for (listener in listeners) {
                    listener.onVideoSizeChange(videoSize.width, videoSize.height)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    for (listener in listeners) {
                        listener.onPlayComplete()
                    }
                } else if (playbackState == Player.STATE_READY) {
                    for (listener in listeners) {
                        listener.onPlayerPrepare()
                    }
                    if (!seekOptimizeEnable) {
                        return
                    }
                    if (!handleSeekComplete) {
                        lastSeekCompleteTime = System.currentTimeMillis()
                        if (targetSeekPosition < 0) {
                            handleSeekComplete = true
                            return
                        }
                        val tolerance = 30
                        val currentPos = exoPlayer.currentPosition
                        if (abs(currentPos - targetSeekPosition) > tolerance) {
                            exoPlayer.seekTo(targetSeekPosition)
                        } else if (targetSeekPosition >= 0) {
                            targetSeekPosition = -1
                            handleSeekComplete = true
                        } else {
                            handleSeekComplete = true
                        }
                    }
                }
            }
        })
    }

    override fun prepare(context: Context, uri: Uri) {
        super.prepare(context, uri)
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
    }

    override fun prepare(adapter: MediaSourceAdapter<SimpleExoPlayer>) {
        super.prepare(adapter)
        exoPlayer.apply {
            adapter.setInputMedia(this)
        }
        exoPlayer.prepare()
    }

    override fun startPlayer(reset: Boolean) {
        try {
            if (reset || exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.playWhenReady = true
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun pausePlayer() {
        if (exoPlayerRelease) {
            return
        }
        try {
            exoPlayer.playWhenReady = false
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun stopPlayer() {
        if (exoPlayerRelease) {
            return
        }
        try {
            exoPlayer.stop()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun release() {
        super.release()
        if (exoPlayerRelease) {
            return
        }
        try {
            exoPlayer.stop()
            exoPlayer.release()
            exoPlayerRelease = true
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun seekTo(millis: Long) {
        if (!seekOptimizeEnable) {
            exoPlayer.seekTo(millis)
            return
        }
        lastSeekRequestTime = System.currentTimeMillis()
        if (lastSeekCompleteTime == 0L) {
            lastSeekCompleteTime = lastSeekRequestTime
        }
        if (!handleSeekComplete) {
            targetSeekPosition = millis
            if (lastSeekRequestTime - lastSeekCompleteTime > 1500) {
                // reset and wait next delay
                lastSeekCompleteTime = lastSeekRequestTime
                exoPlayer.seekTo(millis)
            }
            return
        }
        targetSeekPosition = -1
        handleSeekComplete = false
        exoPlayer.seekTo(millis)
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun setVolume(volume: Float) {
        exoPlayer.volume = volume
    }

    override fun getPlayer(): SimpleExoPlayer {
        return exoPlayer
    }

    override fun setupTextureViewDisplay(textureView: TextureView) {
        exoPlayer.setVideoTextureView(textureView)
    }

    override fun setupSurfaceViewDisplay(surfaceView: SurfaceView) {
        exoPlayer.setVideoSurfaceView(surfaceView)
    }

    override fun setupVideoSurface(surface: Surface) {
        exoPlayer.setVideoSurface(surface)
    }

    override fun isPlaying(): Boolean {
        try {
            return exoPlayer.isPlaying
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    override fun getVolume(): Float {
        return exoPlayer.volume
    }

    override fun getPlaybackSpeed(): Float {
        return exoPlayer.playbackParameters.speed
    }

    override fun getDuration(): Long {
        return exoPlayer.duration
    }

    override fun getPlayPosition(): Long {
        return exoPlayer.currentPosition
    }

    override fun setLooping(loop: Boolean) {
        if (loop) {
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        } else {
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    override fun createNewPlayerCore(): IPlayerCore<SimpleExoPlayer> {
        return ExoPlayerCore(context)
    }
}
