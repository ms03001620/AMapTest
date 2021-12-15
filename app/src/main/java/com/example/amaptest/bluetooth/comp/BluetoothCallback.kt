package com.example.amaptest.bluetooth.comp

interface BluetoothCallback {
    fun onEvent(action: String)
    fun onFoundDevice(address: String){}
    fun onScanFinish()
    fun requestPairing()
    fun onScanStart()
}