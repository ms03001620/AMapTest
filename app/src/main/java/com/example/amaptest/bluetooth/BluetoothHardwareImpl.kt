package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothAdapter

class BluetoothHardwareImpl(val bluetoothAdapter: BluetoothAdapter) : BluetoothHardware {

    override fun bondedDevices() = bluetoothAdapter.bondedDevices

    override fun startDiscovery() = bluetoothAdapter.startDiscovery()

    override fun isDiscovering() = bluetoothAdapter.isDiscovering
}