package com.example.amaptest.marker

import com.amap.api.maps.model.animation.Animation
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import kotlinx.coroutines.sync.Semaphore
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger

class AnimFactory(private val semaphore: Semaphore) {
    private var countTask = AtomicInteger()

    fun createAnimationListener(realListener: Animation.AnimationListener? = null): Animation.AnimationListener {
        countTask.incrementAndGet()

        return object : Animation.AnimationListener {
            override fun onAnimationStart() {
                realListener?.onAnimationStart()
            }

            override fun onAnimationEnd() {
                realListener?.onAnimationEnd()
                val count = countTask.decrementAndGet()
                logd("onAnimationEnd count:${count}", "AnimFactory")
                if (count == 0) {
                    semaphore.release()
                    logd("release true", "AnimFactory")
                }
            }
        }
    }

    fun forceRelease(): Boolean {
        return try {
            semaphore.release()
            logd("forceRelease true", "AnimFactory")
            true
        } catch (e: Exception) {
            loge("forceRelease false", "AnimFactory", e)
            false
        }
    }

    fun tryAcquire() = semaphore.tryAcquire()

    suspend fun acquire() = semaphore.acquire()

}