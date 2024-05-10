package com.example.amaptest.keyboard

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Range

/**
 * 判断用户输入值是否在指定范围内
 * 两次输入或设置相同值不会产生事件
 */
class RangeTextWatcher(
    private val range: Range<Int>,
    private val callback: OnWatcherCallback
) : TextWatcher {

    private var uncheckString = ""
    private var lastValue: Int? = null
    private val handler = Handler(Looper.getMainLooper()) { false }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        uncheckString = s.toString()
        handler.removeCallbacks(checkRunnable)
        handler.postDelayed(checkRunnable, 1000)
    }

    private val checkRunnable = Runnable {
        val input = uncheckString.toIntOrNull()
        if (lastValue != null && lastValue == input) {
            // no change
            return@Runnable
        }

        if (input != null && range.contains(input)) {
            callback.onInRange(input, range)
        } else {
            callback.onOutRange(input, range)
        }
        lastValue = input
    }

    interface OnWatcherCallback {
        fun onOutRange(input: Int?, range: Range<Int>)
        fun onInRange(input: Int, range: Range<Int>)
    }
}
