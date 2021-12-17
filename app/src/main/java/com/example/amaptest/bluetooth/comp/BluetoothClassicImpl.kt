package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import java.lang.Exception

class BluetoothClassicImpl(private val bluetoothAdapter: BluetoothAdapter) : BluetoothDevices {

    override fun bondedDevices(): Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

    override fun startDiscovery() = bluetoothAdapter.startDiscovery()

    override fun cancelDiscovery() = bluetoothAdapter.cancelDiscovery()

    override fun isDiscovering() = bluetoothAdapter.isDiscovering

    override fun bindDevice(address: String?): Int {
        val device = bluetoothAdapter.getRemoteDevice(address)
        if (device?.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        }
        return device?.bondState ?: -1
    }
}