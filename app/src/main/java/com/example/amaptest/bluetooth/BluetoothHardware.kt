package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice
import java.lang.UnsupportedOperationException

interface BluetoothHardware {

    @Throws(UnsupportedOperationException::class)
    fun createAdapter()
    fun isEnable(): Boolean
    fun bondedDevices(): Set<BluetoothDevice>
    fun startDiscovery():Boolean
}