package com.example.amaptest.ble

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.R
import com.example.amaptest.bluetooth.BluetoothHelper
import com.example.amaptest.databinding.ActivityBluetoothLeBinding
import java.lang.RuntimeException

class BleActivity : AppCompatActivity() {
    lateinit var binding: ActivityBluetoothLeBinding
    var logIndex = 0
    val macSet = hashMapOf<String, ScanResult>()

    lateinit var bluetoothAdapter: BluetoothAdapter

    val listener = object : BluetoothHelper.OnBluetoothEvent {
        override fun onErrorNoBluetoothDevice() {
            finish()
        }

        override fun onBondedDevices(bluetoothDevices: Set<BluetoothDevice>) {
            if (bluetoothDevices.isEmpty()) {
                printlnLogs("bluetoothDevices: 0")
            } else {
                bluetoothDevices.forEach {
                    printlnLogs(it.toString())
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_le)
        initRegister()
        initBase {
            initBtns()
            initChecksdk()
            printLocalInfo()
        }
    }

    private fun initBtns() {
        binding.btnInfo.setOnClickListener {
            printLocalInfo()
        }

        binding.btnRequestScan.setOnClickListener {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            bluetoothAdapter.bluetoothLeScanner.startScan(
                filterBuilder,
                settingsBuilder,
                scanCallback
            )
            binding.btnRequestScan.removeCallbacks(removeRunnable)
            binding.btnRequestScan.postDelayed(removeRunnable, 5000)
        }

        binding.btnRequestStop.setOnClickListener {
            binding.btnRequestScan.removeCallbacks(removeRunnable)
            removeRunnable.run()
        }

        binding.btnBindingConnect.setOnClickListener {
            try {
                bluetoothAdapter.getRemoteDevice("3C:06:30:19:C2:DA")?.let {
                    connect(it)
                }
            } catch (e: IllegalArgumentException) {
                printlnLogs("getRemoteDevice: ${e.message}")
            }
        }
    }

    private val settingsBuilder = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setReportDelay(0)
        .build()

    private val filterBuilder = mutableListOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("Time33333333")
            .build()
    )

    private val removeRunnable = Runnable {
        printlnLogs("stopScan")
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

    //time33333 4476205C-4A22-45BC-BC7C-94962223E7B8
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val key = result.device.address
            if (macSet.contains(key)) {
                printlnLogs("Added:$callbackType, result:${result.device} , name:${result.device.name}, size:${macSet.size}")
            } else {
                macSet.put(key, result)
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>) {
            printlnLogs("onBatchScanResults: ${results.size}")
        }

        override fun onScanFailed(errorCode: Int) {
            printlnLogs("onScanFailed: $errorCode")
        }
    }

    private fun connect(bluetoothDevice: BluetoothDevice) {
        bluetoothDevice.connectGatt(this, false, bluetoothGattCallback)
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
                "onConnectionStateChange status(${gattConCode(status)}) -> newState(${
                    profileConCode(
                        newState
                    )
                })"
            )
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            printlnLogs("onServicesDiscovered")
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


    private fun printMain() = printlnLogs("isMain${Looper.getMainLooper() == Looper.myLooper()}")

    private fun printlnLogs(logs: String) {
        var newLogs = logs
        val oldLogs = binding.textLogs.text
        if (oldLogs.isNullOrBlank().not()) {
            newLogs += "\n"
        }
        binding.textLogs.setText("${++logIndex}, $newLogs$oldLogs")
    }


    private fun initRegister() {
    }


    fun printLocalInfo() {
        StringBuilder().let { info ->
            bluetoothAdapter.let { adapter ->
                info.append("本机设备:${adapter.name}")
                info.append("\n\t")
                info.append("蓝牙开关:${adapter.isEnabled}")
                info.append("\n\t")
                info.append("isDiscovering:${adapter.isDiscovering}")
                info.append("\n\t")
                info.append("state:${adapter.state}")
            }
        }.let {
            printlnLogs(it.toString())
        }
    }

    fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    private fun initBase(accessable: () -> Unit) {
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }
            ?.also {
                Toast.makeText(this, "蓝牙LE不可用", Toast.LENGTH_SHORT).show()
                finish()
            }

        val service = getSystemService(Context.BLUETOOTH_SERVICE)
        if (service is BluetoothManager &&
            service.adapter != null &&
            service.adapter.bluetoothLeScanner != null
        ) {
            bluetoothAdapter = service.adapter
            accessable.invoke()
        } else {
            Toast.makeText(this, "蓝牙硬件不可用", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initChecksdk() {
        printlnLogs("Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")
    }


    private fun gattConCode(code: Int): String {
        return when (code) {
            GATT_SUCCESS -> "GATT_SUCCESS"
            GATT_FAILURE -> "GATT_FAILURE"
            else -> "code:$code"
        }
    }


    private fun profileConCode(code: Int): String {
        return when (code) {
            STATE_CONNECTED -> "STATE_CONNECTED"
            STATE_CONNECTING -> "STATE_CONNECTING"
            STATE_DISCONNECTED -> "STATE_DISCONNECTED"
            STATE_DISCONNECTING -> "STATE_DISCONNECTING"
            else -> "code:$code"
        }
    }

}