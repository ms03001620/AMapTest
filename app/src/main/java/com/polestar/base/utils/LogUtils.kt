package com.polestar.base.utils

import android.util.Log
import com.example.amaptest.BuildConfig

const val DEFAULT_TAG="PoleStar"

fun logv(message: String, tag: String = DEFAULT_TAG) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, message)
    }
}

fun logd(message: String, tag: String = DEFAULT_TAG) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, message)
    }
}

fun logi(message: String, tag: String = DEFAULT_TAG) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, message)
    }
}

fun logw(message: String, tag: String = DEFAULT_TAG) {
    if (BuildConfig.DEBUG) {
        Log.v(tag, message)
    }
}

fun loge(message: String, tag: String = DEFAULT_TAG) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message)
    }
}

fun loge(message: String, tag: String = DEFAULT_TAG, throwable: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message, throwable)
    }
}


fun Any?.showLog(prefix:String="",tag: String = DEFAULT_TAG) {
    try {
        this?.let {
            Log.d(tag, "$prefix:$this")
        } ?: let {
            Log.d(tag, "$prefix:null")
        }
    } catch (e: Exception) {
        Log.d(tag, "$prefix:${e.message}")
    }
}