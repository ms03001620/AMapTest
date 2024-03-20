package com.example.amaptest.life

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MyObserver(owner: LifecycleOwner) : DefaultLifecycleObserver {
    private var myOwner = MyOwner(owner)

    fun getOwner() = myOwner

    override fun onResume(owner: LifecycleOwner) {
        myOwner.start()
    }

    override fun onPause(owner: LifecycleOwner) {
        myOwner.stop()
    }
}