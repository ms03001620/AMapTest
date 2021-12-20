package com.example.amaptest.bluetooth.comp

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.amaptest.bluetooth.BluetoothUtils

class BluetoothLeImpl(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothDevices {

    override fun bondedDevices(): Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

    override fun startDiscovery() = bluetoothAdapter.startDiscovery()

    override fun cancelDiscovery() = bluetoothAdapter.cancelDiscovery()

    override fun isDiscovering() = bluetoothAdapter.isDiscovering

    override fun bindDevice(address: String?): Int {
        val device = bluetoothAdapter.getRemoteDevice(address)
        return if (device.bondState == BluetoothDevice.BOND_BONDED) {
            BluetoothDevice.BOND_BONDED
        } else {
            device.connectGatt(context, false, bluetoothGattCallback)
            BluetoothDevice.BOND_BONDING
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            printlnLogs("onPhyUpdate")
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            printlnLogs("onPhyRead")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            printlnLogs(
                "onConnectionStateChange status(${BluetoothUtils.gattConCode(status)}) -> newState(${
                    BluetoothUtils.profileConCode(
                        newState
                    )
                })"
            )

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                printlnLogs("discoverServices ${gatt.discoverServices()}")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            printlnLogs("onServicesDiscovered: status:${BluetoothUtils.gattConCode(status)}")

        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            printlnLogs("onCharacteristicRead")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            printlnLogs("onCharacteristicWrite")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic?
        ) {
            printlnLogs("onCharacteristicChanged")
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            printlnLogs("onDescriptorRead")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            printlnLogs("onDescriptorWrite")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            printlnLogs("onReliableWriteCompleted")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            printlnLogs("onReadRemoteRssi")
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            printlnLogs("onMtuChanged")
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            printlnLogs("onServiceChanged")
        }
    }

    private fun printlnLogs(logs: String) {
        Log.d("BluetoothLeImpl", "printlnLogs:$logs")
    }

}