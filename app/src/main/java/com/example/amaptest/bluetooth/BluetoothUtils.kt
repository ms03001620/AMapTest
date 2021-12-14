package com.example.amaptest.bluetooth

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
}