package com.polestar.base.ext

import android.text.InputFilter
import android.text.Spanned
import android.util.Range

class RangeFilter(
    val range: Range<Int>
) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        println("source = [${source}], start = [${start}], end = [${end}], dest = [${dest}], dstart = [${dstart}], dend = [${dend}]")

        //增加
        return ""

    }
}