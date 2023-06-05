package com.example.amaptest.flow

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NewsRepository {

    var base = 0

    val latestNews: Flow<Int> = flow {
        while(true) {
            Log.d("_____", "emit$base")
            emit(base++)
            delay(1000)
        }
    }
}