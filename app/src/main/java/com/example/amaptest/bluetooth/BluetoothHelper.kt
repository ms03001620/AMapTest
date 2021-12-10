package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice

class BluetoothHelper(
    private val listener: OnBluetoothEvent?,
    private val hardware: BluetoothHardware?
) {
    interface OnBluetoothEvent {
        fun onErrorNoBluetoothDevice()
        fun onBondedDevices(bluetoothDevices: Set<BluetoothDevice>)
    }

    fun requestBondedDevices() {
        hardware?.bondedDevices()?.let {
            listener?.onBondedDevices(it)
        }
    }

    fun requestScan() {
        hardware?.startDiscovery()
    }
}
