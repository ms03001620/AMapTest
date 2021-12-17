package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothDevice

interface BluetoothDevices {

    fun bondedDevices(): Set<BluetoothDevice>
    fun startDiscovery(): Boolean
    fun isDiscovering(): Boolean
    fun bindDevice(address: String?): Int
    fun cancelDiscovery(): Boolean
}