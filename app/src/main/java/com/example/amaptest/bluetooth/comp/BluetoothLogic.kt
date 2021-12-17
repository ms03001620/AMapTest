package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.VisibleForTesting

class BluetoothLogic(
    private val devices: BluetoothDevices,
    private var uiCallback: OnScanEventCallback? = null,
    private val onScanEvent: OnScanEvent
) {
    private var step = TaskStep.SCAN

    init {
        onScanEvent.setCallback(object : BluetoothCallback {
            override fun onEvent(action: String) {
                uiCallback?.onEvent(action)
            }

            override fun onFoundDevice() {
                // found device close scanner immediately
                devices.cancelDiscovery()
                step = TaskStep.BIND
            }

            override fun onScanStart() {
                uiCallback?.onScanStart()
            }

            override fun onBindStatusChange(old: Int, new: Int) {
                when (new) {
                    BluetoothDevice.BOND_BONDED -> {
                        step = TaskStep.BONDED
                        uiCallback?.onBondedSuccess()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        step = TaskStep.BOND_BONDING
                    }
                    BluetoothDevice.BOND_NONE -> {
                        if (old == BluetoothDevice.BOND_BONDING) {
                            step = TaskStep.REQUEST_RETRY
                            // retry when from binding -> bind none
                            uiCallback?.onRequestReBinding()
                        }
                    }
                    else -> {
                        Log.d("BluetoothLogic", "ignore old$old, new:$new")
                    }
                }
            }

            override fun onScanFinish() {
                uiCallback?.onScanFinish()
                if (onScanEvent.address.isNullOrBlank()) {
                    uiCallback?.onNotFound(3)
                } else {
                    doBluetoothTask()
                }
            }

            override fun requestPairing() {
                uiCallback?.onRequestPairing()
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setUiCallback(uiCallback: OnScanEventCallback) {
        this.uiCallback = uiCallback
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getStep() = TaskStep.valueOf(step.name)

    /**
     * scan -> find device -> binding; callback
     */
    fun doBluetoothTask() {
        when (step) {
            TaskStep.SCAN -> {
                if (devices.isDiscovering().not()) {
                    if (devices.startDiscovery().not()) {
                        // start failed
                        uiCallback?.onNotFound(1)
                    }
                }
            }
            TaskStep.BIND -> {
                val result = devices.bindDevice(onScanEvent.address)
                if (result == BluetoothDevice.BOND_BONDED) {
                    // device was BONDED
                    step = TaskStep.BONDED
                    uiCallback?.onBondedSuccess()
                }
                Log.d("BluetoothLogic", "bindDevice:$result")
            }
            else -> {
                Log.d("BluetoothLogic", "ignore:$step")
            }
        }
    }

    fun doRetryBind() {
        step = TaskStep.BIND
        doBluetoothTask()
    }

    fun stop() {
        devices.cancelDiscovery()
    }

    fun registerReceiver(activity: Activity?) {
        onScanEvent.registerReceiver(activity)
    }

    fun unregisterReceiver(activity: Activity?) {
        onScanEvent.unregisterReceiver(activity)
    }
}