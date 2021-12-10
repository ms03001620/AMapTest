package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothAdapter
import java.lang.UnsupportedOperationException

class BluetoothHardwareImpl(val bluetoothAdapter: BluetoothAdapter) : BluetoothHardware {

    override fun isEnable(): Boolean {
        return bluetoothAdapter.isEnabled
    }


    override fun bondedDevices() = bluetoothAdapter.bondedDevices


    // need android.permission.BLUETOOTH_ADMIN.
    override fun startDiscovery() = bluetoothAdapter.startDiscovery()
}