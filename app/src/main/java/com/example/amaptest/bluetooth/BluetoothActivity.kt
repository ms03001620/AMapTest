package com.example.amaptest.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.LocationUtils
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityBluetoothBinding
import java.lang.StringBuilder
import java.util.*

class BluetoothActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[BluetoothViewModel::class.java]
    }

    lateinit var binding: ActivityBluetoothBinding
    var logIndex = 0
    val macSet = hashMapOf<String, BluetoothDevice>()
    val uuid = UUID.nameUUIDFromBytes("Hello".toByteArray(Charsets.UTF_8))

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

    lateinit var device: BluetoothHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth)
        initRegister()
        initBase {
            initHelper()
            initBtns()
            initChecksdk()
            printLocalInfo()
        }
    }

    private fun initBase(accessable: () -> Unit) {
        val service = getSystemService(Context.BLUETOOTH_SERVICE)
        if (service is BluetoothManager && service.adapter != null) {
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

    fun initHelper() {
        device = BluetoothHelper(listener, BluetoothHardwareImpl(bluetoothAdapter))
    }

    private fun initBtns() {
        binding.btnRequestDevice.setOnClickListener {
            // crash S RequestMultiplePermissions; effect api bluetoothAdapter.bondedDevices
            // java.lang.SecurityException: Permission Denial: starting Intent { act=android.bluetooth.adapter.action.REQUEST_ENABLE
            requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        binding.btnInfo.setOnClickListener {
            printLocalInfo()
        }

        binding.btnRequestBonded.setOnClickListener {
            device.requestBondedDevices()
        }
        binding.btnRequestScan.setOnClickListener {
            if (checkLocation()) {
                checkLocationSwitch {
                    device.requestScan()
                }
            } else {
                requestOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

        binding.btnWait.setOnClickListener {
            // 启用可检测性 可被别人扫描匹配到
            val discoverableIntent: Intent =
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                }
            // 系统将显示对话框，请求用户允许将设备设为可检测到模式。如果用户响应“Yes”，则设备会变为可检测到模式，
            // 并在指定时间内保持该模式。然后，您的 Activity 将会收到对 onActivityResult() 回调的调用
            // 其结果代码等于设备可检测到的持续时间。如果用户响应“No”或出现错误，则结果代码为 RESULT_CANCELED。
            startActivity(discoverableIntent)
        }

        binding.btnBindingForce.setOnClickListener {
            val imei = binding.editImei.text.toString()
            if (imei.isBlank()) {
                printlnLogs("need IMEI")
            } else {
                try {
                    bluetoothAdapter.getRemoteDevice(imei)?.let {
                        getNoBondedDevice(it)
                    }?.let {
                        pairToDevice(it)
                    }
                } catch (e: IllegalArgumentException) {
                    printlnLogs("getRemoteDevice: ${e.message}")
                }
            }
        }

        binding.btnBinding.setOnClickListener {
            val imei = binding.editImei.text.toString()
            if (imei.isBlank()) {
                printlnLogs("need IMEI")
            } else {
                macSet[imei.uppercase()]?.let { targetDevice ->
                    bluetoothAdapter.isDiscovering.let {
                        if (it) {
                            printlnLogs("isDiscovering true")
                            if (bluetoothAdapter.cancelDiscovery()) {
                                printlnLogs("cancelDiscovery")
                                targetDevice
                            } else {
                                printlnLogs("cancelDiscovery failed")
                                null
                            }
                        } else {
                            targetDevice
                        }
                    }?.let {
                        getNoBondedDevice(it)
                    }?.let {
                        pairToDevice(it)
                    }

                } ?: run {
                    printlnLogs("not fount device is:$imei, scan first")
                }
            }
        }
    }

    fun getNoBondedDevice(device: BluetoothDevice): BluetoothDevice? {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            return device
        }
        printlnLogs("$device, bonded!!!")
        return null
    }

    fun pairToDevice(bluetoothDevice: BluetoothDevice) {
        printlnLogs("pairToDevice:$bluetoothDevice")
        val create = bluetoothDevice.createBond()
        printlnLogs("createBond:$create")
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val prevState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                    printlnLogs(
                        "onReceive state:${parseToString(prevState)} -> state:${
                            parseToString(
                                state
                            )
                        }"
                    )
                }
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        val macAddress = (device.address ?: "").uppercase()

                        if (macSet.containsKey(macAddress)) {
                            printlnLogs("has contain:$macAddress")
                        } else {
                            macSet.put(macAddress, device)
                            printlnLogs("${device.name}, $macAddress")
                        }
                    } ?: run {
                        printlnLogs("onReceive null BluetoothDevice")
                    }
                }
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let {
                        val macAddress = (device.address ?: "").uppercase()
                        if (macSet.containsKey(macAddress)) {
                            // 名字改变的设备在结果中
                            val oldName = macSet.get(macAddress)?.name ?: ""
                            val newName = device.name ?: ""
                            // 将新设备对象放入集合
                            macSet.put(macAddress, device)
                            printlnLogs("ACTION_NAME_CHANGED ($oldName) -> ($newName)")
                        } else {
                            // 名字变更的设备不在结果中， 直接放入集合
                            macSet.put(macAddress, device)
                            printlnLogs("ACTION_NAME_CHANGED add new device:${device.name}")
                        }
                    }
                }

                /*BluetoothDevice.ACTION_PAIRING_REQUEST->{
                    //

                }*/
                else -> {
                    printlnLogs("onReceive action:${intent.action}, ignore!!!")
                }
            }
        }
    }

    private fun printlnLogs(logs: String) {
        var newLogs = logs
        val oldLogs = binding.textLogs.text
        if (oldLogs.isNullOrBlank().not()) {
            newLogs += "\n"
        }
        binding.textLogs.setText("${++logIndex}, $newLogs$oldLogs")
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                printlnLogs("granted")
            } else {
                //deny
                printlnLogs("deny")
            }
        }


    private fun initRegister() {
        //ACTION_CONNECTION_STATE_CHANGED 连接变化
        IntentFilter().apply {
            this.addAction(BluetoothDevice.ACTION_FOUND)
            this.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            this.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            this.addAction(BluetoothDevice.ACTION_NAME_CHANGED) // 远程设备名称更新
            this.addAction(ACTION_DISCOVERY_STARTED) // 开始扫描
            this.addAction(ACTION_DISCOVERY_FINISHED) // 扫描结束
        }.let {
            registerReceiver(receiver, it)
        }
    }

    fun checkLocation(): Boolean {
        val t = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return t == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationSwitch(callback: () -> Unit) {
        if (LocationUtils.isLocationSwitchOpen(this)) {
            // 有权限进入
            callback.invoke()
        } else {
            // 显示权限请求对话框
            LocationUtils.goLocationServiceSettingForBluetooth(this) {
                // 无权限进入
                printlnLogs("用户取消授权")
            }
        }
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

    private fun parseToString(code: Int): String {
        return when (code) {
            10 -> "BOND_NONE"
            11 -> "BOND_BONDING"
            12 -> "BOND_BONDED"
            else -> "code:$code"
        }
    }

    private var requestOnlyFinePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                device.requestScan()
            } else {
                with(allGrants.keys.toString() + allGrants.values.toString()) {
                    Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}