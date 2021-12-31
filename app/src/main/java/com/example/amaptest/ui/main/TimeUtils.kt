package com.example.amaptest.ui.main

import android.text.TextUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    const val DATE_FORMAT_YMDHMSMS_WITH_T = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val DATE_FORMAT_DEFAULT_HMS_2 = "yyyy-MM-dd HH:mm:ss"


    /**
     *         val str = "2021-12-30T00:55:14.000+00:00"
    val str1 = "2001-07-04T12:08:56.235-07:00"
    //2021-12-30T00:55:14.000+00:00

    logd("aaa:${TimeUtils.UTCToLocal(str1)}", "______")
     */

    fun UTCToLocal(
        utcDatetime: String?,
        utcFormatStr: String = DATE_FORMAT_YMDHMSMS_WITH_T,
        targetFormatStr: String = DATE_FORMAT_DEFAULT_HMS_2,
    ): String? {
        if (TextUtils.isEmpty(utcDatetime)) {
            return ""
        }
        val utcFormater = SimpleDateFormat(utcFormatStr, Locale.CHINA) //UTC时间格式
        utcFormater.timeZone = TimeZone.getTimeZone("UTC")
        var gpsUTCDate: Date? = null
        var format = utcDatetime ?: ""
        try {
            gpsUTCDate = utcFormater.parse(utcDatetime)
            val localFormater = SimpleDateFormat(targetFormatStr, Locale.CHINA) //当地时间格式
            localFormater.timeZone = TimeZone.getDefault()
            format = localFormater.format(gpsUTCDate?.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return format
    }
}