package com.example.amaptest.bluetooth

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.CommonAskDialog
import com.example.amaptest.R
import com.example.amaptest.bluetooth.comp.BluetoothLogic
import com.example.amaptest.bluetooth.comp.BluetoothUiCallback
import com.example.amaptest.databinding.ActivityBluetoothSampleBinding

class BluetoothSampleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothSampleBinding
    private lateinit var  bluetoothLogic: BluetoothLogic
    private val permissionHelper = BluetoothPermissionHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_sample)

        permissionHelper.checkFeature {
            ininBluetoothLogic()
            initRegister()
        }
    }

    private fun initRegister() {
        bluetoothLogic.registerReceiver(this)
    }

    private fun ininBluetoothLogic() {
        val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: TEST_DEVICE_NAME
        bluetoothLogic = BluetoothLogic(
            deviceName,
            NAME_MATCH_LENGTH,
            BluetoothHardwareImpl(permissionHelper.getAdapter()),
            bluetoothCallback
        )
    }

    val bluetoothCallback = object : BluetoothUiCallback {
        override fun onEvent(action: String) {
            printlnLogs(action)
        }

        override fun onNotFound(reasonCode: Int) {
            printlnLogs("onNotFound reason:$reasonCode")
            showRetryScanDialog(this@BluetoothSampleActivity, leftCallback = {
            }, rightCallback = {
                permissionHelper.attemptRunCallback {
                    bluetoothLogic.doBluetoothTask()
                }
            })
        }

        override fun requestPairing() {
            printlnLogs("requestPairing")
        }

        override fun onScanFinish() {
            printlnLogs("onScanFinish")
        }

        override fun onScanStart() {
            printlnLogs("onScanStart")
        }

        override fun onBondedSuccess() {
            printlnLogs("onBondedSuccess")
        }

        override fun onRequestReBinding() {
            printlnLogs("onRetry")
            showRetryDialog(this@BluetoothSampleActivity, leftCallback = {
                Toast.makeText(applicationContext, "已放弃绑定", Toast.LENGTH_SHORT).show()
            }, rightCallback = {
                permissionHelper.attemptRunCallback {
                    bluetoothLogic.doRetryBind()
                }
            })
        }
    }


    override fun onResume() {
        super.onResume()
        doBluetoothTask()
    }

    private fun doBluetoothTask(){
        permissionHelper.attemptRunCallback {
            bluetoothLogic.doBluetoothTask()
        }
    }

    var logIndex = 0
    private fun printlnLogs(logs: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalAccessError("not ui thread")
        }
        var newLogs = logs
        val oldLogs = binding.textLogs.text
        if (oldLogs.isNullOrBlank().not()) {
            newLogs += "\n"
        }
        binding.textLogs.setText("${++logIndex}, $newLogs$oldLogs")
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionHelper.checkFeature {
            bluetoothLogic.unregisterReceiver(this)
            bluetoothLogic.stop()
        }
    }

    private fun showRetryDialog(
        context: Context,
        leftCallback: (() -> Unit)? = null,
        rightCallback: (() -> Unit)? = null
    ) {
        CommonAskDialog.Builder(
            context,
            "重试",
            "放弃",
            leftCallback = leftCallback
        ).create(
            "重新绑定蓝牙设备", listener = { rightCallback?.invoke() })
    }

    private fun showRetryScanDialog(
        context: Context,
        leftCallback: (() -> Unit)? = null,
        rightCallback: (() -> Unit)? = null
    ) {
        CommonAskDialog.Builder(
            context,
            "重新搜索",
            "放弃",
            leftCallback = leftCallback
        ).create(
            "重新搜索蓝牙设备", listener = { rightCallback?.invoke() })
    }

    companion object{
        const val EXTRA_DEVICE_NAME = "intent_extra_device_name"
        const val TEST_DEVICE_NAME  = "Mark的MacBook Pro"
        const val NAME_MATCH_LENGTH = 16
    }
}