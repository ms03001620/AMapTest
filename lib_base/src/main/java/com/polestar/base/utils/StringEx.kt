package com.polestar.base.utils

import java.math.RoundingMode

fun String?.numberToString(): String {
    try {
        val string = java.math.BigDecimal(this).stripTrailingZeros().toPlainString()
        val bigDecimal = java.math.BigDecimal(string)
        return if (bigDecimal.scale() == 0) {
            bigDecimal.toInt().toString()
        } else if (bigDecimal.scale() == 1) {
            bigDecimal.setScale(1, RoundingMode.HALF_UP).toString()
        } else {
            bigDecimal.setScale(2, RoundingMode.HALF_UP).toString()
        }
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    return "0"
}

fun String?.numberToStringWithSign(): String {
    return "¥ ${this?.numberToString()}"
}