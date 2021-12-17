package com.example.amaptest.bluetooth.comp

interface OnScanEventCallback {
    fun onEvent(action: String) {}
    fun onFoundDevice(address: String) {}
    fun onNotFound(reasonCode: Int) {}
    fun requestPairing() {}
    fun onScanFinish() {}
    fun onScanStart() {}
    fun onRequestReBinding(){}
    fun onBondedSuccess() {}
}