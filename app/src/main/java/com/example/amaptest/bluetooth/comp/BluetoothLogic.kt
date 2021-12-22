package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothDevice
import androidx.annotation.VisibleForTesting
import com.example.amaptest.bluetooth.comp.OnScanEventCallback.Companion.REASON_EMPTY_RESULT
import com.example.amaptest.bluetooth.comp.OnScanEventCallback.Companion.REASON_START_FAILED
import com.polestar.base.utils.logd
import com.polestar.base.utils.loge
import java.lang.Exception

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
                step = TaskStep.BIND
                devices.cancelDiscovery()
            }

            override fun onScanStart() {
                uiCallback?.onScanStart()
            }

            override fun onBindStatusChange(old: Int, new: Int) {
                when (new) {
                    BluetoothDevice.BOND_BONDED -> {
                        step = TaskStep.BONDED
                        uiCallback?.onBondedSuccess(onScanEvent.address)
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        step = TaskStep.BOND_BONDING
                        if (old == BluetoothDevice.BOND_NONE) {
                            uiCallback?.onRequestPairing()
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        if (old == BluetoothDevice.BOND_BONDING) {
                            step = TaskStep.REQUEST_RETRY
                            // retry when from binding -> bind none
                            uiCallback?.onRequestReBinding()
                        }
                    }
                    else -> {
                        logd("ignore old$old, new:$new", TAG)
                    }
                }
            }

            override fun onScanFinish() {
                if (onScanEvent.address.isNullOrBlank()) {
                    step = TaskStep.SCAN_FINISHED
                    uiCallback?.onScanFinish()
                    uiCallback?.onNotFound(REASON_EMPTY_RESULT)
                } else {
                    step = TaskStep.BIND
                    doBluetoothTask()
                }
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
        try {
            when (step) {
                TaskStep.SCAN -> {
                    if (devices.isDiscovering().not()) {
                        if (devices.startDiscovery().not()) {
                            // start failed
                            uiCallback?.onNotFound(REASON_START_FAILED)
                        }
                    }
                }
                TaskStep.BIND -> {
                    val result = devices.bindDevice(onScanEvent.address)
                    if (result == BluetoothDevice.BOND_BONDED) {
                        // device was BONDED
                        step = TaskStep.BONDED
                        uiCallback?.onBondedSuccess(onScanEvent.address)
                    }
                    logd("bindDevice:$result", TAG)
                }
                else -> {
                    logd("ignore:$step", TAG)
                }
            }
        } catch (e: Exception) {
            loge("doBluetoothTask", TAG, e)
            when (step) {
                TaskStep.SCAN -> uiCallback?.onNotFound(REASON_START_FAILED)
                else -> uiCallback?.onRequestReBinding()
            }
        }
    }

    fun doRetryScan() {
        step = TaskStep.SCAN
        doBluetoothTask()
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

    companion object{
        const val TAG = "BluetoothLogic"
    }
}