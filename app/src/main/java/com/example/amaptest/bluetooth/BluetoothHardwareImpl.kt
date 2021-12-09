package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothAdapter
import java.lang.UnsupportedOperationException

class BluetoothHardwareImpl : BluetoothHardware {
    lateinit var bluetoothAdapter: BluetoothAdapter

    @Throws(UnsupportedOperationException::class)
    override fun createAdapter() {
        BluetoothAdapter.getDefaultAdapter()?.let {
            bluetoothAdapter = it
        } ?: run {
            throw UnsupportedOperationException("no bluetooth device")
        }
    }

    override fun isEnable(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    fun sss(){
    }

    override fun bondedDevices() = bluetoothAdapter.bondedDevices


    // need android.permission.BLUETOOTH_ADMIN.
    override fun startDiscovery() = bluetoothAdapter.startDiscovery()


}