package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.example.amaptest.bluetooth.BluetoothDevices

class BluetoothLogic(
    private val deviceName: String,
    private val nameMatchLength: Int,
    private val devices: BluetoothDevices,
    private var uiCallback: BluetoothUiCallback? = null,
    private val scanCenter: ScanCenter = BluetoothEventCenter(nameMatchLength, deviceName)
) {
    private var step = TaskStep.SCAN

    init {
        scanCenter.setCallback(object : BluetoothCallback {
            override fun onEvent(action: String) {
                uiCallback?.onEvent(action)
            }

            override fun onFoundDevice(address: String) {
                // found device close scanner immediately
                devices.cancelDiscovery()
                scanCenter.address = address
                step = TaskStep.BIND
            }

            override fun onScanStart() {
                uiCallback?.onScanStart()
            }

            override fun onBindStatusChange(old: Int, new: Int) {
                when (new) {
                    BluetoothDevice.BOND_BONDED -> {
                        step = TaskStep.BONDED
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
                scanCenter.address?.let {
                    doBluetoothTask()
                } ?: run {
                    uiCallback?.onNotFound(3)
                }
            }

            override fun requestPairing() {
                uiCallback?.requestPairing()
            }
        })
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun setUiCallback(uiCallback: BluetoothUiCallback) {
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
                        // 启动扫描失败
                        uiCallback?.onNotFound(1)
                    }
                }
            }
            TaskStep.BIND -> {
                val result = devices.bindDevice(scanCenter.address)
                Log.d("BluetoothLogic", "bindDevice:$result")
            }
            TaskStep.REQUEST_RETRY -> {
                // wait user click retry
                Log.d("BluetoothLogic", "REQUEST_RETRY")
            }
        }
    }

    fun doRetryBind() {
        step = TaskStep.BONDED
        doBluetoothTask()
    }

    fun stop(){
        devices.cancelDiscovery()
    }

    fun registerReceiver(activity: Activity) {
        //ACTION_CONNECTION_STATE_CHANGED 连接变化
        IntentFilter().apply {
            this.addAction(BluetoothDevice.ACTION_UUID)
            this.addAction(BluetoothDevice.ACTION_FOUND)
            this.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            this.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            this.addAction(BluetoothDevice.ACTION_NAME_CHANGED) // 远程设备名称更新
            this.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) // 开始扫描
            this.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // 扫描结束
        }.let {
            activity.registerReceiver(scanCenter.receiver, it)
        }
    }

    fun unregisterReceiver(activity: Activity) {
        activity.unregisterReceiver(scanCenter.receiver)
    }

}