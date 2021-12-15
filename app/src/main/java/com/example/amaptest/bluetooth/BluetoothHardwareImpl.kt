package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothAdapter
import java.lang.Exception

class BluetoothHardwareImpl(val bluetoothAdapter: BluetoothAdapter) : BluetoothDevices {

    override fun bondedDevices() = bluetoothAdapter.bondedDevices

    override fun startDiscovery() = bluetoothAdapter.startDiscovery()

    override fun cancelDiscovery() = bluetoothAdapter.cancelDiscovery()

    override fun isDiscovering() = bluetoothAdapter.isDiscovering

    override fun bindDevice(address: String?):Boolean {
        return try {
            bluetoothAdapter.getRemoteDevice(address).createBond()
        } catch (e: Exception) {
            false
        }
    }
}