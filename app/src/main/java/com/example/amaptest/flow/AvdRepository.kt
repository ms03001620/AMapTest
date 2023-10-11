package com.example.amaptest.flow

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import kotlin.random.Random

class AvdRepository {

    val indexFlow: Flow<Int> = flow {
        while (true) {
            val index = Random.nextInt(5)
            Log.d("AvdRepository", "randomIndex:$index")
            emit(index)
            delay(10000)
        }
    }

    val uuidFlow = MutableSharedFlow<String>(
        replay = 1,// 1为解决最小化恢复后可以获取上一个值，否则为null，必须重新生成
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    suspend fun createUuid() {
        val string = UUID.randomUUID().toString()
        Log.d("AvdRepository", "update:$string")
        uuidFlow.emit(string)
    }
}