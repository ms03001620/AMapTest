package com.example.amaptest.sync

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AsyncToSyncModel : ViewModel() {
    interface SingleMethodCallback {
        fun onCallBack(value: String)
    }

    private fun runTask(callback: SingleMethodCallback) {
        thread  {
            Thread.sleep(500)
            callback.onCallBack("result")
        }
    }

    private fun runTaskDefault() {
        runTask(object : SingleMethodCallback {
            override fun onCallBack(value: String) {
            }
        })
    }

    fun runTaskWithSuspend() = runBlocking {
         suspendCoroutine { continuation ->
            runTask(object : SingleMethodCallback {
                override fun onCallBack(value: String) {
                    continuation.resume(value)
                }
            })
        }
    }

}