package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice

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
}