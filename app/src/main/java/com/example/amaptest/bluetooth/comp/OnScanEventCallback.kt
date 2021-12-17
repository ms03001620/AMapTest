package com.example.amaptest.bluetooth.comp

interface OnScanEventCallback {
    fun onEvent(action: String) {}
    fun onNotFound(reasonCode: Int) {}
    fun onRequestPairing() {}
    fun onScanFinish() {}
    fun onScanStart() {}
    fun onRequestReBinding(){}
    fun onBondedSuccess() {}

    companion object{
        const val REASON_START_FAILED = 1
        const val REASON_EMPTY_RESULT = 3
    }
}