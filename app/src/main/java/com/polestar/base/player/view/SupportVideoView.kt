package com.polestar.base.player.view

import android.content.Context
import android.net.Uri
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.SurfaceView
import android.view.TextureView
import com.example.amaptest.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource

import com.polestar.base.player.core.IPlayerCore
import com.polestar.base.player.core.MainThreadCoreWrapper
import com.polestar.base.player.core.MediaSourceAdapter
import com.polestar.base.player.core.PlayerListener
import com.polestar.base.player.core.SimplePlayerListener
import com.polestar.base.player.impl.PlayerCoreFactory
import com.polestar.base.player.utils.ExoCacheFactory
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by lwh 2021/9/1 15:36
 * 视频播放器简单封装, 带有播放失败的fallback机制
 */
class SupportVideoView : GoogleAspectRatioFrameLayout {
    private lateinit var playCore: IPlayerCore<*>
    private var surfaceView: SurfaceView? = null
    private var textureView: TextureView? = null
    private var scaleType: Int = ENUM_SCALE_FIT_CENTER
    private var playerCoreType: Int = ENUM_CORE_EXO_PLAYER
    private var containerType: Int = ENUM_CONTAINER_SURFACE_VIEW
    private var settingLooping = false
    private var playerRelease = false
    private var singleMediaSource: Uri? = null
    private var playerChangeCallback: PlayerChangeCallback? = null
    private var enablePlayerFallback: Boolean = true
    private val changePlayerList = mutableListOf(
        ENUM_CORE_EXO_PLAYER,
        ENUM_CORE_MEDIA_PLAYER
    )

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SupportVideoView, 0, 0)
        containerType = ta.getInt(R.styleable.SupportVideoView_container_type, ENUM_CONTAINER_SURFACE_VIEW)
        playerCoreType = ta.getInt(R.styleable.SupportVideoView_player_core, ENUM_CORE_EXO_PLAYER)
        scaleType = ta.getInt(R.styleable.SupportVideoView_scale_type, ENUM_SCALE_FIT_CENTER)
        ta.recycle()
        when (scaleType) {
            ENUM_SCALE_CENTER_CROP -> {
                setResizeModeInner(RESIZE_MODE_ZOOM)
            }
            ENUM_SCALE_FIT_CENTER -> {
                setResizeModeInner(RESIZE_MODE_FIT)
            }
            ENUM_SCALE_FIT_XY -> {
                setResizeModeInner(RESIZE_MODE_FILL)
            }
        }
        if (isInEditMode) {
            return
        }
        configPlayerCore(playerCoreType)
        configContainer(containerType)
    }

    private fun configPlayerCore(playerCoreType: Int) {
        playCore = when (playerCoreType) {
            ENUM_CORE_EXO_PLAYER -> {
                PlayerCoreFactory.createExoPlayerCore(context)
            }
            ENUM_CORE_MEDIA_PLAYER -> {
                PlayerCoreFactory.createMediaPlayerCore(context)
            }
            else -> {
                throw RuntimeException("unknown playCoreType:$playCore")
            }
        }
        changePlayerList.remove(playerCoreType)
        playCore.addPlayerListener(object : SimplePlayerListener() {
            override fun onVideoSizeChange(width: Int, height: Int) {
                super.onVideoSizeChange(width, height)
                setAspectRatio(width / height.toFloat())
            }

            override fun onPlayEncounterError(ex: Exception?) {
                super.onPlayEncounterError(ex)
                val cause = ex?.cause?.cause
                if (cause is UnknownHostException || cause is SocketTimeoutException) {
                    Timber.i("network error, skip change player.")
                    return
                }
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    togglePlayer()
                } else {
                    post {
                        togglePlayer()
                    }
                }
            }
        })
        playerChangeCallback?.onPlayerCoreChange(playCore)
    }

    private fun configContainer(containerType: Int) {
        when (containerType) {
            ENUM_CONTAINER_SURFACE_VIEW -> {
                surfaceView = SurfaceView(context).apply {
                    addView(
                        this,
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    )
                    playCore.setupSurfaceViewDisplay(this)
                }
            }
            ENUM_CONTAINER_TEXTURE_VIEW -> {
                textureView = TextureView(context).apply {
                    addView(
                        this,
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    )
                    playCore.setupTextureViewDisplay(this)
                }
            }
        }
    }

    private fun togglePlayer() {
        if (!enablePlayerFallback) {
            return
        }
        if (changePlayerList.isEmpty()) {
            return
        }
        val listeners = playCore.getPlayerListeners()
        playCore.release()
        configPlayerCore(changePlayerList.first())
        for (listener in listeners) {
            playCore.addPlayerListener(listener)
        }
        removeAllViews()
        configContainer(containerType)
        setLooping(settingLooping)
        startPlayer()
    }

    fun setVideoSource(uri: Uri) {
        singleMediaSource = uri
    }

    fun setLooping(loop: Boolean) {
        settingLooping = loop
        playCore.setLooping(loop)
    }

    /**
     * @param cacheRemote 仅支持exoplayer缓存
     */
    @Suppress("UNCHECKED_CAST")
    fun startPlayer(cacheRemote: Boolean = false) {
        if (playCore.isPlaying()) {
            playCore.stopPlayer()
        }
        singleMediaSource?.let {
            if (playerRelease) {
                playerRelease = false
                resetPlayer()
            }
            if (cacheRemote && playerCoreType == ENUM_CORE_EXO_PLAYER) {
                val exoPlayerCore = playCore as MainThreadCoreWrapper<SimpleExoPlayer>
                exoPlayerCore.prepare(object : MediaSourceAdapter<SimpleExoPlayer> {
                    override fun setInputMedia(player: SimpleExoPlayer) {
                        val mediaItem = MediaItem.fromUri(it)
                        val mediaSource = ProgressiveMediaSource
                            .Factory(ExoCacheFactory.getCacheFactory(context.applicationContext))
                            .createMediaSource(mediaItem)
                        player.setMediaSource(mediaSource)
                    }
                })
            } else {
                playCore.prepare(context, it)
            }
            playCore.startPlayer(false)
        }
    }

    fun resetPlayer() {
        val listeners = playCore.getPlayerListeners()
        configPlayerCore(playerCoreType)
        for (listener in listeners) {
            playCore.addPlayerListener(listener)
        }
        if (surfaceView != null) {
            playCore.setupSurfaceViewDisplay(surfaceView!!)
        } else if (textureView != null) {
            playCore.setupTextureViewDisplay(textureView!!)
        }
    }

    fun resumePlayer() {
        playCore.startPlayer(false)
    }

    fun pausePlayer() {
        playCore.pausePlayer()
    }

    fun stopPlayer() {
        playCore.stopPlayer()
    }

    fun release() {
        playCore.release()
        playerRelease = true
    }

    fun isPlaying(): Boolean {
        return playCore.isPlaying()
    }

    fun addPlayerListener(listener: PlayerListener) {
        playCore.addPlayerListener(listener)
    }

    fun getPlayerCore(): IPlayerCore<*> {
        return playCore
    }

    fun setEnablePlayerFallback(enable: Boolean) {
        enablePlayerFallback = enable
    }

    fun setPlayerChangeCallback(callback: PlayerChangeCallback) {
        this.playerChangeCallback = callback
        callback.onPlayerCoreChange(playCore)
    }

    interface PlayerChangeCallback {
        fun onPlayerCoreChange(playerCore: IPlayerCore<*>)
    }

    companion object {
        private const val TAG = "LocalVideoView"

        // surface type
        const val ENUM_CONTAINER_SURFACE_VIEW = 0
        const val ENUM_CONTAINER_TEXTURE_VIEW = 1

        // player core
        const val ENUM_CORE_EXO_PLAYER = 0
        const val ENUM_CORE_MEDIA_PLAYER = 1

        // scale type
        const val ENUM_SCALE_FIT_CENTER = 0
        const val ENUM_SCALE_CENTER_CROP = 1
        const val ENUM_SCALE_FIT_XY = 2
    }
}
