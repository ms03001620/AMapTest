package com.example.amaptest.pager

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.TextView
import com.example.amaptest.R

class ResendTextView : CountdownTextView {
    interface OnRestoreTime {
        fun onNoStoreData()
    }

    private val textSend: String
    private val textSending: String

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        //"Resend" -en
        // 重新发送 -zh
        textSend = resources.getString(R.string.sms_resend)
        //"Resend（%ss）" -en
        // 重新发送（%ss） -zh
        textSending = resources.getString(R.string.sms_resending)


        if (isValid()) { // have old second
            isEnabled = false
            val second = restoreCountByTime()
            setRunningText(second)
        }
    }

    fun runStoreTime() {
        runStoreTime(null)
    }

    fun runStoreTime(onRestoreTime: OnRestoreTime?) {
        if (isValid()) {
            init()
        } else {
            onRestoreTime?.onNoStoreData()
        }
    }

    fun init() {
        init(DEF_COUNT_SECOND, null)
    }

    fun init(second: Long = DEF_COUNT_SECOND, firstInit: (() -> Unit)? = null) {
        initAndStart(second, object : Callback {
            override fun onCountdown(second: Long, isLast: Boolean) {
                if (isLast) {
                    isEnabled = true
                    setIdleText()
                } else {
                    isEnabled = false
                    setRunningText(second)
                }
            }

            override fun onFirstInit() {
                firstInit?.invoke()
            }

            override fun onClear() {
                onCountdown(0, true)
            }
        })
    }

    //"Resend"
    fun setIdleText() {
        text = textSend
        setTextColor(getColor(R.color.color_8001FE))
    }

    //"Resend（60s）"
    fun setRunningText(second: Long) {
        text = String.format(textSending, second.toString())
        setTextColor(getColor(R.color.default_black_color))
        applyColorString(getThis(), textSend, R.color.color_3D0A0400)
    }

    private fun getThis() = this

    private fun getColor(resId: Int) = context.resources.getColor(resId)

    private fun applyColorString(content: TextView, target: String, targetColor: Int) {
        val fullString = content.text.toString()
        val start = fullString.indexOf(target)
        val end = start + target.length
        if (end != 0 && start != -1) {
            val style = SpannableStringBuilder()
            style.append(fullString)
            //设置部分文字颜色
            val foregroundColorSpan = ForegroundColorSpan(content.resources.getColor(targetColor))
            style.setSpan(foregroundColorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            content.text = style
        }
    }

    companion object {
        const val DEF_COUNT_SECOND = 60L
    }
}