package com.example.amaptest.pager

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

/**
 * 上下滑动手势监听
 */
class UpDownFlingListener(private val callback: (Finger) -> Unit) : GestureDetector.SimpleOnGestureListener() {
    private val distanceY = 300
    private val distanceXLimit = 400

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val distanceUp = event1.y - event2.y
        val distanceX = event2.x - event1.x
        Log.v(TAG, "offsetY:${distanceUp}" + "offsetX:${distanceX}")

        // not x scroll
        if (abs(distanceX) < distanceXLimit) {
            if (distanceUp > distanceY) {
                Log.v(TAG, "up")
                callback.invoke(Finger.UP)
            }
            if (distanceUp < -distanceY) {
                Log.v(TAG, "down")
                callback.invoke(Finger.DOWN)
            }
        }
        return true
    }

    companion object {
        const val TAG = "UpFlingGestureListener"
    }
}

enum class Finger {
    UP, DOWN
}