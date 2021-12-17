package com.example.amaptest.bluetooth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.CommonAskDialog
import com.example.amaptest.R
import com.example.amaptest.bluetooth.comp.*
import com.example.amaptest.databinding.ActivityBluetoothSampleBinding

class BluetoothSampleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothSampleBinding
    private lateinit var  bluetoothLogic: BluetoothLogic
    private val permissionHelper =
        BluetoothPermissionHelper(this, object : BluetoothPermissionHelper.OnEnterSettingPage {
            override fun onEnterPositionSetting() {
                showAlertDialog(
                    getActivity(),
                    R.string.permission_prompt_location,
                    leftCallback = {
                        Toast.makeText(getActivity(), "已取消定位授权", Toast.LENGTH_LONG).show()
                    },
                    rightCallback = {
                        try {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (e: Exception) {
                            startActivity(Intent(Settings.ACTION_SETTINGS))
                        }
                    })
            }

            override fun onEnterNearbySetting() {
                showAlertDialog(
                    getActivity(),
                    R.string.permission_prompt_bluetooth,
                    leftCallback = {
                        Toast.makeText(getActivity(), "已取消蓝牙授权", Toast.LENGTH_LONG).show()
                    },
                    rightCallback = {
                        try {
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            ).let {
                                startActivity(it)
                            }
                        } catch (e: Exception) {
                            //ignore
                        }
                    })
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("_____", "onCreate")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_sample)

        permissionHelper.checkFeature {
            ininBluetoothLogic()
        }
    }


    private fun ininBluetoothLogic() {
        val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: TEST_DEVICE_NAME
        bluetoothLogic = BluetoothLogic(
/*            BluetoothClassicImpl(permissionHelper.getAdapter()),*/
            BluetoothLeImpl(this, permissionHelper.getAdapter()),
            uiCallback,
            BroadcastScanEvent(NAME_MATCH_LENGTH, deviceName)
        )
        bluetoothLogic.registerReceiver(this)
    }

    private fun getActivity() = this

    private val uiCallback = object : OnScanEventCallback {
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

        override fun onRequestPairing() {
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
        Log.d("_____", "onResume")
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

    private fun showAlertDialog(
        context: Context,
        messageResId: Int,
        leftCallback: (() -> Unit)? = null,
        rightCallback: (() -> Unit)? = null
    ) {
        CommonAskDialog.Builder(
            context,
            context.getString(R.string.charging_open_settings),
            context.getString(R.string.cancel_option),
            leftCallback = leftCallback
        ).create(
            context.getString(messageResId), listener = { rightCallback?.invoke() })
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