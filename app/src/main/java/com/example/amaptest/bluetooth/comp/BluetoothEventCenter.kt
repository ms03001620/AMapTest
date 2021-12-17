package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.util.Log
import com.example.amaptest.bluetooth.BluetoothUtils

class BluetoothEventCenter(
    private val nameMatchLength: Int,
    private val deviceName: String
) : OnScanEvent() {

    var flagForDiscovery = ""

    override val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val prevState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                    bluetoothCallback?.onBindStatusChange(prevState, state)
                    printlnLogs(
                        "onReceive state:${parseToString(prevState)} -> state:${
                            parseToString(
                                state
                            )
                        }"
                    )
                }
                BluetoothDevice.ACTION_FOUND -> {
                    printlnLogs("ACTION_FOUND")
                    checkDevice(intent)
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    printlnLogs("ACTION_NAME_CHANGED")
                    checkDevice(intent)
                }
                BluetoothDevice.ACTION_UUID -> {
                    printlnLogs("ACTION_UUID")
                    val uuid = intent.getParcelableExtra<ParcelUuid>(BluetoothDevice.EXTRA_UUID)
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    printlnLogs("ACTION_UUID :${uuid}, device:${device?.name ?: ""}")
                }
                BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                    bluetoothCallback?.requestPairing()
                    // https://blog.csdn.net/zrf1335348191/article/details/54020225/
                    printlnLogs("ACTION_PAIRING_REQUEST")
                    val type = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PAIRING_VARIANT,
                        BluetoothDevice.ERROR
                    )

                    when (type) {
                        BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION -> {
                            printlnLogs("ACTION_PAIRING_REQUEST type: PASSKEY_CONFIRMATION")
                        }
                        BluetoothDevice.PAIRING_VARIANT_PIN -> {
                            printlnLogs("ACTION_PAIRING_REQUEST type: PAIRING_VARIANT_PIN")
                        }
                        else -> {
                            printlnLogs("ACTION_PAIRING_REQUEST type:$type")
                        }
                    }

                    val pairKey =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY, BluetoothDevice.ERROR)
                    printlnLogs("ACTION_PAIRING_REQUEST pairKey:$pairKey")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    if (flagForDiscovery != BluetoothAdapter.ACTION_DISCOVERY_STARTED) {
                        flagForDiscovery = BluetoothAdapter.ACTION_DISCOVERY_STARTED
                        bluetoothCallback?.onScanStart()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (flagForDiscovery != BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                        flagForDiscovery = BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                        bluetoothCallback?.onScanFinish()
                    }
                }
                else -> {
                    printlnLogs("onReceive action:${intent.action}, ignore!!!")
                }
            }
        }
    }

    override fun registerReceiver(activity: Activity?) {
        // ACTION_CONNECTION_STATE_CHANGED 连接变化
        activity?.registerReceiver(receiver, IntentFilter().apply {
            this.addAction(BluetoothDevice.ACTION_UUID)
            this.addAction(BluetoothDevice.ACTION_FOUND)
            this.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            this.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            this.addAction(BluetoothDevice.ACTION_NAME_CHANGED) // 远程设备名称更新
            this.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED) // 开始扫描
            this.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) // 扫描结束
        })
    }

    override fun unregisterReceiver(activity: Activity?) {
        activity?.unregisterReceiver(receiver)
    }

    private fun parseToString(code: Int): String {
        return BluetoothUtils.parseToString(code)
    }

    fun checkDevice(intent: Intent) {
        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        device?.let {
            val address = it.address
            if (isTargetDevice(device, deviceName)) {
                super.address = address
                bluetoothCallback?.onFoundDevice()
            }
        }
    }


    private fun isTargetDevice(device: BluetoothDevice?, deviceName: String): Boolean {
        printlnLogs("isTargetDevice: $deviceName, ${device?.name}")
        return BluetoothUtils.calcSameBitAtTile(device?.name, deviceName) >= nameMatchLength
    }

    private fun printlnLogs(log: String) {
        Log.d("printlnLogs:", log)
    }
}