package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import com.example.amaptest.bluetooth.comp.UUU
import java.lang.reflect.Method

object BluetoothUtils {
    fun calcSameBitAtTile(source: String?, target: String?): Int {
        if (source.isNullOrBlank() || target.isNullOrBlank()) {
            return 0
        }
        var count = 0

        var sourceLastIndex = source.lastIndex
        var targetLastIndex = target.lastIndex

        while (sourceLastIndex >= 0 && targetLastIndex >= 0) {
            if (source[sourceLastIndex] == target[targetLastIndex]) {
                count++
                sourceLastIndex--
                targetLastIndex--
            } else {
                break
            }
        }
        return count
    }

    fun parseToString(code: Int): String {
        return when (code) {
            BluetoothDevice.BOND_NONE -> "BOND_NONE"
            BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
            BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
            else -> "code:$code"
        }
    }

    fun profileConCode(code: Int): String {
        return when (code) {
            BluetoothGatt.STATE_CONNECTED -> "STATE_CONNECTED"
            BluetoothGatt.STATE_CONNECTING -> "STATE_CONNECTING"
            BluetoothGatt.STATE_DISCONNECTED -> "STATE_DISCONNECTED"
            BluetoothGatt.STATE_DISCONNECTING -> "STATE_DISCONNECTING"
            else -> "code:$code"
        }
    }


    fun gattConCode(code: Int): String {
        return when (code) {
            BluetoothGatt.GATT_SUCCESS -> "GATT_SUCCESS"
            BluetoothGatt.GATT_FAILURE -> "GATT_FAILURE"
            else -> "code:$code"
        }
    }

    fun removeBond(device: BluetoothDevice): Boolean {
        return UUU.unpairDevice(device)
/*        return try {
            device::class.java.getMethod("removeBond").invoke(device)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }*/
    }

    private fun unpairDevice(device: BluetoothDevice):Boolean {
        try {
            val m: Method = device.javaClass
                .getMethod("removeBond", null)
            m.invoke(device, null as Array<Any?>?)
            return true
        } catch (e: java.lang.Exception) {
            return false
        }
    }
}