package com.example.amaptest.bluetooth.comp

interface BluetoothUiCallback {
    fun onEvent(action: String) {}
    fun onFoundDevice(address: String) {}
    fun onNotFound(reasonCode: Int) {}
    fun requestPairing() {}
    fun onScanFinish() {}
    fun onScanStart() {}
}