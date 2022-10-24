package com.example.amaptest.flow

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentLinkedQueue

class QueueRepository {
    private val queue = ConcurrentLinkedQueue<String>()
    var base = 0

    val itemsFlow: Flow<String> = flow {
        while(true) {
            if (queue.isNotEmpty()) {
                val element = queue.poll()
                Log.d("_____", "poll :$element")
                if (element != null) {
                    emit(element)
                }
            }
            Log.d("_____", "delay")
            delay(1000)
        }
    }

    fun addItem(element: String){
        queue.offer(element)
    }
}