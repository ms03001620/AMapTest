package com.polestar.charging.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager


object ContextUtils {
    //https://juejin.cn/post/6949002128530604062/
    fun getChannelName(context: Context): String {
        val applicationInfo: ApplicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        return applicationInfo.metaData.getString("CHANNEL", "dev")
    }
}