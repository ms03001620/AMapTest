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

    private var successInit = false

    fun init() {
        try {
            hardware?.createAdapter()
            successInit = true
        } catch (e: Exception) {
            listener?.onErrorNoBluetoothDevice()
        }

    }

    fun requestBondedDevices() {
        if (successInit) {
            hardware?.bondedDevices()?.let {
                listener?.onBondedDevices(it)
            }
        }
    }



    fun requestScan() {
        if (successInit) {
            hardware?.startDiscovery()
        }
    }
}
