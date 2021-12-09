package com.example.amaptest.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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

class BluetoothActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[BluetoothViewModel::class.java]
    }

    lateinit var binding: ActivityBluetoothBinding
    var logIndex = 0

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

    val device = BluetoothHardwareImpl()
    val helper = BluetoothHelper(listener, device)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth)
        initBtns()
        initObserve()
        initStart()
        initHelper()
        initReg()
        initChecksdk()
    }

    private fun initChecksdk() {
        printlnLogs("Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")
    }

    fun initHelper() {
        helper.init()
    }

    private fun initBtns() {
        binding.btnRequestDevice.setOnClickListener {
            // crash S RequestMultiplePermissions; effect api bluetoothAdapter.bondedDevices
            // java.lang.SecurityException: Permission Denial: starting Intent { act=android.bluetooth.adapter.action.REQUEST_ENABLE
            requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        //android.permission.BLUETOOTH_CONNECT
        binding.btnIsEnable.setOnClickListener {
            val t = BluetoothAdapter.getDefaultAdapter()
            var result = "false"

            if (t?.isEnabled == true) {
                result = "true"
            }
            printlnLogs("enabled: $result")
        }

        binding.btnRequestBonded.setOnClickListener {
            helper.requestBondedDevices()
        }
        binding.btnRequestScan.setOnClickListener {
            if (checkLocation()) {
                checkLocationSwitch({
                    helper.requestScan()
                })
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
            startActivity(discoverableIntent)
        }
    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name
                val deviceHardwareAddress = device?.address // MAC address

                printlnLogs("$deviceName, $deviceHardwareAddress")
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

    private fun initObserve() {

    }

    private fun initStart() {

    }

    private fun initReg() {
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    fun checkLocation(): Boolean {
        val t = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return t == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationSwitch(callback: ()->Unit) {
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

    private var requestOnlyFinePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                helper.requestScan()
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