package com.example.amaptest

import android.graphics.Rect
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import com.example.amaptest.pager.ResendTextView

object TouchUtils {
    fun addTouchAreaSize(target: View, buffSize: Int) = target.doOnPreDraw {
        val parent = target.parent
        //parent 必须有足够的区域用来扩展。如果紧紧包裹着这个target那么是无效的
        //如果再尝试使用parent.parent则需要计算出多次嵌套后target的相对位置
        if (parent is ViewGroup) {
            val delegateArea = Rect()
            target.getHitRect(delegateArea)
            if (delegateArea.isEmpty) {
                Log.d("TouchUtils", "isEmpty")
            }
            Log.d("TouchUtils", "target$delegateArea")
            delegateArea.bottom += buffSize
            delegateArea.top -= buffSize
            delegateArea.left -= buffSize
            delegateArea.right += buffSize
            parent.touchDelegate = TouchDelegate(delegateArea, target)
        }
    }
}