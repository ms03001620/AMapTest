package com.example.amaptest.pager

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import java.util.concurrent.TimeUnit

class CountdownTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    interface Callback{
        fun onCountdown(remaining: Long)
    }

    private var callback: Callback? = null
    private var endTimeMs = 0L
    private var sleepTimeMs = 0L
    private val handlerTimer = Handler(Looper.getMainLooper()) { message ->
        Log.d(TAG, "handleMessage:${message.what}")
        if (message.what == MSG_WHAT) {
            calcAndSetText()
            sendNextCountdown()
        }
        false
    }
    private fun calcAndSetText() {
        val now = System.currentTimeMillis()
        val time = if (sleepTimeMs - now > 0) {
            sleepTimeMs
        } else {
            endTimeMs
        }

        val current = time - System.currentTimeMillis()//now
        text = format(current)
        callback?.onCountdown(current)
    }

    private fun sendNextCountdown() {
        handlerTimer.sendEmptyMessageDelayed(MSG_WHAT, 1000)
    }

    private fun start() {
        handlerTimer.sendEmptyMessage(MSG_WHAT)
    }

    private fun stop() {
        handlerTimer.removeMessages(MSG_WHAT)
    }

    fun setCountdownAndStart(endMs: Long, sleepTimeMs: Long = 0L) {
        this.endTimeMs = endMs
        this.sleepTimeMs = sleepTimeMs;
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "onVisibilityChanged visibility:$visibility")
        if (visibility == View.VISIBLE) {
            start()
        } else {
            stop()
        }
    }

    fun setCallback(callback: Callback){
        this.callback = callback
    }

    private fun format(ms: Long): String {
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(ms) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
            TimeUnit.MILLISECONDS.toSeconds(ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }

    companion object {
        const val TAG = "CountdownTextView"
        const val MSG_WHAT = 10010
    }
}