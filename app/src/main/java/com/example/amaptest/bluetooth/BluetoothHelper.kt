package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice

class BluetoothHelper(
    private val listener: OnBluetoothEvent?,
    private val devices: BluetoothDevices?
) {
    interface OnBluetoothEvent {
        fun onErrorNoBluetoothDevice()
        fun onBondedDevices(bluetoothDevices: Set<BluetoothDevice>)
    }

    fun requestBondedDevices() {
        devices?.bondedDevices()?.let {
            listener?.onBondedDevices(it)
        }
    }

    fun requestScan() {
        devices?.startDiscovery()
    }
}
