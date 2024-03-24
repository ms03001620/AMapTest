package com.example.amaptest.life

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class NfcObserver(activityOwner: LifecycleOwner) : DefaultLifecycleObserver {
    private var nfcOwner = NfcOwner(activityOwner)

    fun getNfcOwner() = nfcOwner

    override fun onResume(owner: LifecycleOwner) {
        nfcOwner.enableOperateNfc()
    }

    override fun onPause(owner: LifecycleOwner) {
        nfcOwner.disableOperateNfc()
    }
}