package com.polestar.base.player.impl

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import com.polestar.base.player.core.BasePlayerCore
import com.polestar.base.player.core.IPlayerCore
import com.polestar.base.player.core.MediaSourceAdapter
import timber.log.Timber

/**
 * Created by lwh 2021/9/1 11:40
 */
class MediaPlayerCore(context: Context) : BasePlayerCore<MediaPlayer>(context), Handler.Callback {
    private val mediaPlayer: MediaPlayer
    private var isPrepared = false
    private var playWhenReady = false
    private var createdSurface: Surface? = null
    private var isSettingContainer = false
    private val progressPoster: Handler by lazy {
        Handler(Looper.getMainLooper(), this)
    }

    init {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                for (listener in listeners) {
                    listener.onPlayComplete()
                    listener.onPlayerStateChange(false)
                }
            }
            setOnVideoSizeChangedListener { _, width, height ->
                for (listener in listeners) {
                    listener.onVideoSizeChange(width, height)
                }
            }
            setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    for (listener in listeners) {
                        listener.onRenderFirstFrame()
                        listener.onPlayerStateChange(true)
                    }
                }
                false
            }
            setOnErrorListener { _, _, _ ->
                for (listener in listeners) {
                    listener.onPlayEncounterError(null)
                }
                false
            }
            setOnPreparedListener {
                isPrepared = true
                if (playWhenReady) {
                    startPlayer(false)
                }
                for (listener in listeners) {
                    listener.onPlayerPrepare()
                }
            }
        }
    }

    override fun prepare(context: Context, uri: Uri) {
        super.prepare(context, uri)
        try {
            isPrepared = false
            mediaPlayer.reset()
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.prepareAsync()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun prepare(adapter: MediaSourceAdapter<MediaPlayer>) {
        super.prepare(adapter)
        try {
            adapter.setInputMedia(mediaPlayer)
            mediaPlayer.prepareAsync()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun startPlayer(reset: Boolean) {
        playWhenReady = true
        if (!isPrepared) {
            return
        }
        if (isSettingContainer && createdSurface == null) {
            // will be invoke by surfaceCreated
            return
        }
        try {
            mediaPlayer.start()
            if (reset) {
                mediaPlayer.seekTo(0)
            }
            progressPoster.removeMessages(MESSAGE_GET_PROGRESS)
            progressPoster.sendEmptyMessage(MESSAGE_GET_PROGRESS)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun pausePlayer() {
        playWhenReady = false
        if (!isPrepared) {
            return
        }
        try {
            mediaPlayer.pause()
            for (listener in listeners) {
                listener.onPlayerStateChange(false)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        progressPoster.removeMessages(MESSAGE_GET_PROGRESS)
    }

    override fun stopPlayer() {
        isPrepared = false
        try {
            mediaPlayer.stop()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun release() {
        super.release()
        isPrepared = false
        try {
            mediaPlayer.release()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        progressPoster.removeMessages(MESSAGE_GET_PROGRESS)
    }

    override fun seekTo(millis: Long) {
        if (!isPrepared) {
            return
        }
        try {
            mediaPlayer.seekTo(millis.toInt())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        Timber.tag("MediaPlayerCore").e("not supported")
    }

    override fun setVolume(volume: Float) {
        if (!isPrepared) {
            return
        }
        try {
            mediaPlayer.setVolume(volume, volume)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun setLooping(loop: Boolean) {
        mediaPlayer.isLooping = loop
    }

    override fun getPlayer(): MediaPlayer {
        return mediaPlayer
    }

    override fun setupTextureViewDisplay(textureView: TextureView) {
        isSettingContainer = true
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                setupVideoSurface(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
    }

    override fun setupSurfaceViewDisplay(surfaceView: SurfaceView) {
        isSettingContainer = true
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                setupVideoSurface(holder.surface)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                createdSurface = null
            }
        })
    }

    override fun setupVideoSurface(surface: Surface) {
        createdSurface = surface
        if (!isRelease() && surface.isValid) {
            mediaPlayer.setSurface(surface)
        }
        if (isPrepared && playWhenReady) {
            startPlayer(false)
        }
    }

    override fun isPlaying(): Boolean {
        try {
            return mediaPlayer.isPlaying
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    override fun getVolume(): Float {
        return 1f
    }

    override fun getPlaybackSpeed(): Float {
        return 1f
    }

    override fun getDuration(): Long {
        return mediaPlayer.duration.toLong()
    }

    override fun getPlayPosition(): Long {
        return try {
            mediaPlayer.currentPosition.toLong()
        } catch (ex: Exception) {
            ex.printStackTrace()
            0
        }
    }

    override fun createNewPlayerCore(): IPlayerCore<MediaPlayer> {
        return MediaPlayerCore(context)
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == MESSAGE_GET_PROGRESS) {
            val playPosition = getPlayPosition()
            for (listener in listeners) {
                listener.onVideoFrameRendering(playPosition * 1000L)
            }
            progressPoster.sendEmptyMessageDelayed(MESSAGE_GET_PROGRESS, 40)
        }
        return true
    }

    companion object {
        private const val MESSAGE_GET_PROGRESS = 154261
    }
}
