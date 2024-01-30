package com.polestar.base.player.core

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView

/**
 * Created by lwh 2021/9/1 11:57
 */
class MainThreadCoreWrapper<PlayerInstance>(
    private val delegate: IPlayerCore<PlayerInstance>
) : IPlayerCore<PlayerInstance> {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun prepare(context: Context, uri: Uri) {
        ensureOnMainThread {
            delegate.prepare(context, uri)
        }
    }

    override fun prepare(adapter: MediaSourceAdapter<PlayerInstance>) {
        ensureOnMainThread {
            delegate.prepare(adapter)
        }
    }

    override fun startPlayer(reset: Boolean) {
        ensureOnMainThread {
            delegate.startPlayer(reset)
        }
    }

    override fun pausePlayer() {
        ensureOnMainThread {
            delegate.pausePlayer()
        }
    }

    override fun stopPlayer() {
        ensureOnMainThread {
            delegate.stopPlayer()
        }
    }

    override fun release() {
        ensureOnMainThread {
            delegate.release()
        }
    }

    override fun seekTo(millis: Long) {
        ensureOnMainThread {
            delegate.seekTo(millis)
        }
    }

    override fun setPlaybackSpeed(speed: Float) {
        ensureOnMainThread {
            delegate.setPlaybackSpeed(speed)
        }
    }

    override fun setVolume(volume: Float) {
        ensureOnMainThread {
            delegate.setVolume(volume)
        }
    }

    override fun setLooping(loop: Boolean) {
        ensureOnMainThread {
            delegate.setLooping(loop)
        }
    }

    override fun getPlayer(): PlayerInstance {
        return delegate.getPlayer()
    }

    override fun addPlayerListener(listener: PlayerListener) {
        delegate.addPlayerListener(listener)
    }

    override fun removePlayerListener(listener: PlayerListener) {
        delegate.removePlayerListener(listener)
    }

    override fun getPlayerListeners(): List<PlayerListener> {
        return delegate.getPlayerListeners()
    }

    override fun setupTextureViewDisplay(textureView: TextureView) {
        ensureOnMainThread {
            delegate.setupTextureViewDisplay(textureView)
        }
    }

    override fun setupSurfaceViewDisplay(surfaceView: SurfaceView) {
        ensureOnMainThread {
            delegate.setupSurfaceViewDisplay(surfaceView)
        }
    }

    override fun setupVideoSurface(surface: Surface) {
        ensureOnMainThread {
            delegate.setupVideoSurface(surface)
        }
    }

    override fun isPlaying(): Boolean {
        return delegate.isPlaying()
    }

    override fun getVolume(): Float {
        return delegate.getVolume()
    }

    override fun getPlaybackSpeed(): Float {
        return delegate.getPlaybackSpeed()
    }

    override fun isCallPrepare(): Boolean {
        return delegate.isCallPrepare()
    }

    override fun isRelease(): Boolean {
        return delegate.isRelease()
    }

    override fun getDuration(): Long {
        return delegate.getDuration()
    }

    override fun getPlayPosition(): Long {
        return delegate.getPlayPosition()
    }

    override fun createNewPlayerCore(): IPlayerCore<PlayerInstance> {
        return delegate.createNewPlayerCore()
    }

    private fun ensureOnMainThread(runnable: () -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(runnable)
        } else {
            runnable()
        }
    }

    override fun toString(): String {
        return delegate.toString()
    }
}
