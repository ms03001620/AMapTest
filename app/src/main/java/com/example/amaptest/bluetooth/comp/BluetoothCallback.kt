package com.example.amaptest.bluetooth.comp

interface BluetoothCallback {
    fun onEvent(action: String)
    fun onFoundDevice() {}
    fun onScanFinish()
    fun onScanStart()
    fun onBindStatusChange(old: Int, new: Int)
}