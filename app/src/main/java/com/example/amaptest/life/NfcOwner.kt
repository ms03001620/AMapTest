package com.example.amaptest.life

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class NfcOwner(parent: LifecycleOwner) : LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry

    init {
        lifecycleRegistry = LifecycleRegistry(parent)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun enableOperateNfc() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun disableOperateNfc() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}
