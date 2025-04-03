package com.example.amaptest.stateless

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View.OnClickListener
import android.view.ViewConfiguration
import kotlin.math.abs

class ClickHelper(val context: Context, private val onClickListener: OnClickListener) {
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private var mDownTime: Long = 0L
    private var mIsPossiblyTap: Boolean = false
    private val mTouchSlop: Int
    private val mTapTimeout: Long

    init {
        // 获取系统配置值
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
        mTapTimeout = 150
    }

    fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = ev.x
                mDownY = ev.y
                mDownTime = SystemClock.uptimeMillis()
                mIsPossiblyTap = true // 按下时，总有可能是点击
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 如果移动距离超过阈值，就不再是点击了
                if (mIsPossiblyTap) {
                    val dx = abs(ev.x - mDownX)
                    val dy = abs(ev.y - mDownY)
                    if (dx > mTouchSlop || dy > mTouchSlop) {
                        mIsPossiblyTap = false
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (mIsPossiblyTap) {
                    val eventTime = SystemClock.uptimeMillis()
                    // 检查按下和抬起的时间是否在点击超时范围内
                    if (eventTime - mDownTime <= mTapTimeout) {
                        onClickListener.onClick(null)
                    }
                }
                // 重置状态并消费事件
                mIsPossiblyTap = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                // 手势取消，重置状态并消费事件
                mIsPossiblyTap = false
                return true
            }
        }
        // 对于其他未处理的事件，也消费掉，避免泄露给子视图
        return true
    }
}