package com.example.amaptest.marker

import com.amap.api.maps.model.animation.Animation
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import kotlinx.coroutines.sync.Semaphore

class AnimFactory(val countDownLatch: Semaphore? = null) {
    private var countTask = 0

    fun createAnimationListener(realListener: Animation.AnimationListener? = null): Animation.AnimationListener {
        countTask++

        return object : Animation.AnimationListener {
            override fun onAnimationStart() {
                realListener?.onAnimationStart()
            }

            override fun onAnimationEnd() {
                realListener?.onAnimationEnd()
                countTask--
                loge("onAnimationEnd count:${countTask}", "AnimFactory")
                if (isEmpty()) {
                    countDownLatch?.release()
                }
            }
        }
    }

    private fun isEmpty() = countTask == 0

    fun tryRelease(): Boolean {
        val result = if (isEmpty()) {
            countDownLatch?.release()
            true
        } else {
            false
        }
        logd(" tryRelease:$result", "AnimFactory")
        return result
    }

    suspend fun acquire() = countDownLatch?.acquire()


}