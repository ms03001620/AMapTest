package com.example.amaptest.marker

import com.amap.api.maps.model.animation.Animation
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import com.polestar.base.utils.logv
import kotlinx.coroutines.sync.Semaphore
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger

class AnimFactory(private val semaphore: Semaphore) {
    private val countTask = AtomicInteger()

    fun createAnimationListener(realListener: Animation.AnimationListener? = null): Animation.AnimationListener {
        countTask.incrementAndGet()

        return object : Animation.AnimationListener {
            override fun onAnimationStart() {
                //logd("onAnimationStart", "AnimFactory")
                realListener?.onAnimationStart()
            }

            override fun onAnimationEnd() {
                realListener?.onAnimationEnd()
                val count = countTask.decrementAndGet()
                logv("onAnimationEnd count:${count}", "AnimFactory")
                if (count == 0) {
                    semaphore.release()
                    logv("release true", "AnimFactory")
                }
            }
        }
    }

    fun release(): Boolean {
        return try {
            semaphore.release()
            //logd("forceRelease true", "AnimFactory")
            true
        } catch (e: Exception) {
            loge("forceRelease false", "AnimFactory", e)
            false
        }
    }

    suspend fun acquire(){
        semaphore.acquire()
        countTask.set(0)
    }

}