package com.example.amaptest.pager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView

open class CountdownTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    interface Callback{
        fun onCountdown(second: Long, isLast: Boolean)
        fun onFirstInit()
        fun onClear()
    }
    private var callback: Callback? = null
    private var count = DEF_COUNT

    fun initAndStart(second: Long, callback: Callback) {
        this.callback = callback
        val endTime = System.currentTimeMillis() + second * 1000

        if (haveStoreData()) {
            if (isInvalid()) {
                setEndTime(endTime) // init
                count = second
                callback.onFirstInit()
            } else {
                count = restoreCountByTime()
                Log.w(TAG, "duplicate init")
            }
        } else {
            setEndTime(endTime) // init
            count = second
            callback.onFirstInit()
        }
        start()
    }

    fun haveStoreData() = SPUtils.contains(context, Constants.SP_SMS_DELAY)

    private val handlerTimer = Handler(Looper.getMainLooper()) { message ->
        if (message.what == MSG_COUNTDOWN) {
            val end = getEndTime()
            val now = System.currentTimeMillis()
            count -= 1
            val left = end - now
            Log.d(TAG, "handleMessage:${count}, ${left}")
            if (left <= 0 || count <= 0) {
                callback?.onCountdown(0, true)// ms
                resetData()
            } else {
                callback?.onCountdown(count, false)// ms
                nextCountdown()
            }
        }
        false
    }

    fun restoreCountByTime() = (getEndTime() - System.currentTimeMillis()) / 1000 // restore by time

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE) {
            handlerTimer.removeCallbacksAndMessages(null)
        } else {
            if (count != DEF_COUNT) {
                count = restoreCountByTime()
                start()
            }
        }
    }

    fun isValid() = isInvalid().not()

    fun isInvalid(): Boolean {
        return getEndTime() < System.currentTimeMillis()// to old
    }

    private fun setEndTime(t: Long){
        SPUtils.put(context, Constants.SP_SMS_DELAY, t)
    }

    private fun getEndTime() = SPUtils.get(context, Constants.SP_SMS_DELAY, -1L) as Long

    private fun nextCountdown() {
        handlerTimer.removeMessages(MSG_COUNTDOWN)
        handlerTimer.sendEmptyMessageDelayed(MSG_COUNTDOWN, 1000)
    }

    private fun resetData() {
        handlerTimer.removeCallbacksAndMessages(null)
        SPUtils.remove(context, Constants.SP_SMS_DELAY)
        count = DEF_COUNT
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    fun start() {
        nextCountdown()
    }

    fun stop() {
        handlerTimer.removeCallbacksAndMessages(null)
    }

    fun clear() {
        resetData()
        callback?.onClear()
    }

    companion object {
        const val DEF_COUNT = -1L
        const val TAG = "CountdownTextView"
        const val MSG_COUNTDOWN = 10010
    }
}