package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.IntentFilter
import com.example.amaptest.bluetooth.BluetoothHardware

class BluetoothLogic(
    private val deviceName: String,
    private val bluetoothAdapter: BluetoothHardware,
    private val bluetoothCallback: BluetoothCallback? = null,
    private val bluetoothEventCenter: ScanCenter = BluetoothEventCenter(deviceName)
) {
    init {
        bluetoothEventCenter.setCallback(object : BluetoothCallback {
            override fun onEvent(action: String) {
                bluetoothCallback?.onEvent(action)
            }

            override fun onFoundDevice(address: String) {
                bluetoothEventCenter.address = address
                step = TaskStep.BIND
            }
        })
    }

    var step = TaskStep.SCAN

    /**
     * scan -> find device -> binding; callback
     */
    fun doBluetoothTask() {
        when (step) {
            TaskStep.SCAN -> {
                if (bluetoothAdapter.isDiscovering().not()) {
                    bluetoothAdapter.startDiscovery()
                }
            }
            TaskStep.BIND -> {

            }
        }
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
            activity.registerReceiver(bluetoothEventCenter.receiver, it)
        }
    }

    fun unregisterReceiver(activity: Activity) {
        activity.unregisterReceiver(bluetoothEventCenter.receiver)
    }

    private fun initRegister() {

    }
}