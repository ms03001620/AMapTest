package com.example.amaptest.marker

import com.amap.api.maps.model.animation.Animation
import com.polestar.base.utils.loge
import java.util.concurrent.CountDownLatch

class AnimFactory(val countDownLatch: CountDownLatch) {
    var countTask = 0


    fun createAnimationListener(realListener: Animation.AnimationListener? = null): Animation.AnimationListener {
        countTask++

        return object : Animation.AnimationListener {
            override fun onAnimationStart() {
                realListener?.onAnimationStart()
            }

            override fun onAnimationEnd() {
                realListener?.onAnimationEnd()
                countTask--
                loge("AnimFactory onAnimationEnd count:${countTask}", "MarkerAction")
                if (countTask == 0) {
                    countDownLatch.countDown()
                }
            }
        }
    }
}