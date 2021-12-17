package com.example.amaptest.bluetooth.comp

interface OnScanEventCallback {
    fun onEvent(action: String) {}
    fun onFoundDevice(address: String) {}
    fun onNotFound(reasonCode: Int) {}
    fun onRequestPairing() {}
    fun onScanFinish() {}
    fun onScanStart() {}
    fun onRequestReBinding(){}
    fun onBondedSuccess() {}
}