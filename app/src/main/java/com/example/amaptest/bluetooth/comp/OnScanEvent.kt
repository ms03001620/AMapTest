package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.content.BroadcastReceiver

open class OnScanEvent {
    open val receiver: BroadcastReceiver? = null

    open var address: String? = null

    var bluetoothCallback: BluetoothCallback? = null

    fun setCallback(callback: BluetoothCallback) {
        bluetoothCallback = callback
    }

    fun getCallback() = bluetoothCallback

    open fun registerReceiver(activity: Activity?) {}

    open fun unregisterReceiver(activity: Activity?) {}
}