package com.example.amaptest.bluetooth.comp

import android.content.BroadcastReceiver

open class ScanCenter {
    open val receiver: BroadcastReceiver? = null

    open var address: String? = null

    var bluetoothCallback: BluetoothCallback? = null

    fun setCallback(callback: BluetoothCallback) {
        bluetoothCallback = callback
    }

    fun getCallback() = bluetoothCallback
}