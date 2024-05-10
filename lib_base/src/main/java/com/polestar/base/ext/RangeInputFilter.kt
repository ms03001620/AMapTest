package com.polestar.base.ext

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(
    private val minValue: Int,
    private val maxValue: Int,
    val callback: (String) -> Unit,
    val callOutRange:(String)-> Unit,
) : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val newValue = dest.subSequence(0, dstart).toString() + source.subSequence(
                start,
                end
            ) + dest.subSequence(dend, dest.length)

            if (newValue.isEmpty()) {
                callOutRange("0")
                return "0"
            }

            if (newValue.length > 1 && newValue.startsWith("0")) {
                callback(newValue.subSequence(1, newValue.length).toString())
            }

            if (newValue.toInt() > maxValue) {
                callOutRange("$maxValue")
                callback(maxValue.toString())
            }

            return null
        } catch (t: Throwable) {
            return ""
        }

    }
}
