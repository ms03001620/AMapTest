package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.example.amaptest.bluetooth.BluetoothDevices
import java.lang.Exception

class BluetoothHardwareImpl(val bluetoothAdapter: BluetoothAdapter) : BluetoothDevices {

    override fun bondedDevices() = bluetoothAdapter.bondedDevices

    override fun startDiscovery() = bluetoothAdapter.startDiscovery()

    override fun cancelDiscovery() = bluetoothAdapter.cancelDiscovery()

    override fun isDiscovering() = bluetoothAdapter.isDiscovering

    override fun bindDevice(address: String?): Int {
        return try {
            val device = bluetoothAdapter.getRemoteDevice(address)
            if (device?.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
            }
            return device?.bondState ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            -2
        }
    }
}