package com.example.amaptest.bluetooth.comp

interface BluetoothCallback {
    fun onEvent(action: String)
    fun onFoundDevice(address: String){}

}