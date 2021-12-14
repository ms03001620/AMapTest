package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BluetoothScanner {

    fun getReceiver() = receiver

    val scanMap = hashMapOf<String, String>()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val name = device.name
                        if (name.isNullOrBlank().not()) {
                            if (scanMap.containsKey(name).not()) {
                                val macAddress = (device.address ?: "").uppercase()
                                scanMap.put(name, macAddress)
                            }
                        }
                    }
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val name = device.name
                        if (name.isNullOrBlank().not()) {
                            val macAddress = (device.address ?: "").uppercase()
                            scanMap.put(name, macAddress)
                        }
                    }
                }
            }
        }
    }

    fun containMac(mac: String) = scanMap.containsValue(mac)

    fun getMac(name: String) = scanMap.get(name)
}