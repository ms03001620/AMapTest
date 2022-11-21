package com.example.amaptest

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup

object TouchUtils {
    fun addTouchAreaSize(target: View, buffSize: Int): Boolean {
        val parent = target.parent
        if (parent is ViewGroup) {
            val delegateArea = Rect()
            target.getHitRect(delegateArea)
            if (delegateArea.isEmpty) {
                return false
            }
            delegateArea.bottom += buffSize
            delegateArea.top -= buffSize
            delegateArea.left -= buffSize
            delegateArea.right += buffSize
            parent.touchDelegate = TouchDelegate(delegateArea, target)
            return true
        }
        return false
    }
}