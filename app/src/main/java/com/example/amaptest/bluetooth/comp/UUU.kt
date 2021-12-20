package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothDevice
import java.lang.Exception

object UUU {
    fun unpairDevice(device: BluetoothDevice?): Boolean {
        return try {
            val removeBondMethod = BluetoothDevice::class.java.getMethod("removeBond")
            val result = removeBondMethod.invoke(device)
            if (result is Boolean) {
                result
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}