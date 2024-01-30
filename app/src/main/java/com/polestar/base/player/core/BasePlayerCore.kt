package com.polestar.base.player.core

import android.content.Context
import android.net.Uri
import androidx.annotation.CallSuper
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by lwh 2021/9/2 11:19
 */
abstract class BasePlayerCore<PlayerInstance>(
    val context: Context
) : IPlayerCore<PlayerInstance> {

    private val releaseState = AtomicBoolean()
    private val callPrepare = AtomicBoolean()
    protected val listeners = CopyOnWriteArrayList<PlayerListener>()

    @CallSuper
    override fun release() {
        releaseState.set(true)
    }

    override fun isRelease(): Boolean {
        return releaseState.get()
    }

    @CallSuper
    override fun prepare(context: Context, uri: Uri) {
        callPrepare.set(true)
    }

    @CallSuper
    override fun prepare(adapter: MediaSourceAdapter<PlayerInstance>) {
        callPrepare.set(true)
    }

    override fun addPlayerListener(listener: PlayerListener) {
        this.listeners.add(listener)
    }

    override fun removePlayerListener(listener: PlayerListener) {
        this.listeners.remove(listener)
    }

    override fun getPlayerListeners(): List<PlayerListener> {
        return ArrayList(listeners)
    }

    override fun isCallPrepare(): Boolean {
        return callPrepare.get()
    }
}
