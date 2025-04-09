package com.example.amaptest.stateless

import com.example.amaptest.stateless.KeepViewModel.DeviceCreate.OnInValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class KeepViewModel(
    private val scope: CoroutineScope,
) {
    private val requestSharedFlow = MutableSharedFlow<Request>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 6
    )
    private val deviceHolder = DeviceHolder()

    init {
        scope.launch {
            requestSharedFlow.collect { req ->
                val device = deviceHolder.getDevice()
                val resp = device.getResponse(req)
                req.postResp(resp)
            }
        }
    }

    fun postRequest(req: Request) = scope.launch {
        requestSharedFlow.emit(req)
    }

    class DeviceHolder {
        var device: Device? = null

        val creator = DeviceCreate(object : OnInValid {
            override fun onValid() {
                device = null
            }
        })

        fun getDevice(): Device {
            val current = device

            if (current?.isActive() == true) {
                return current
            } else {
                val newDevice = creator.create()
                device = newDevice
                return newDevice
            }
        }
    }

    class DeviceCreate(val callback: OnInValid) {
        interface OnInValid {
            fun onValid()
        }

        fun create(): Device {
            return Device()
        }
    }


    class Device {
        fun getResponse(req: Request): Response {
            return Response(req.id)
        }

        fun isActive() = true
    }

    class Request(val id: String, val callback: (Response) -> Unit) {
        fun postResp(resp: Response) {
            callback(resp)
        }
    }

    class Response(val id: String) {
        override fun toString(): String {
            return "Response id:$id"
        }
    }
}