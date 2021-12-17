package com.example.amaptest.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.amaptest.LocationUtils

class BluetoothPermissionHelper(
    private val activity: AppCompatActivity,
    private val onEnterSettingPage: OnEnterSettingPage? = null
) {
    interface OnEnterSettingPage {
        fun onEnterPositionSetting()
        fun onEnterNearbySetting()
    }
    private var callback: (() -> Unit)? = null

    private var requestMultiplePermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.keys.isNotEmpty()) {
                if (allGrants.values.all { it }) {
                    attemptGotoBluetoothLePage()
                } else {
                    onEnterSettingPage?.onEnterPositionSetting()
                }
            }
        }

    private var requestMultipleBPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.keys.isNotEmpty()) {
                if (allGrants.values.all { it }) {
                    attemptGotoBluetoothLePage()
                } else {
                    onEnterSettingPage?.onEnterNearbySetting()
                }
            }
        }

    private var requestBluetooth =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                //granted
                attemptGotoBluetoothLePage()
            }
        }

    fun attemptRunCallback(callback: (() -> Unit)?) {
        this.callback = callback
        callback?.let {
            attemptGotoBluetoothLePage()
        }
    }

    private fun attemptGotoBluetoothLePage() {
        checkFeature {
            checkBluetoothPermission {
                checkBluetoothSwitch {
                    checkBluetoothLocationPermission {
                        checkBluetoothLocationSwitch {
                            callback?.invoke()
                        }
                    }
                }
            }
        }
    }

    fun checkFeature(callback: () -> Unit) {
        if (activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        ) {
            callback.invoke()
        } else {
            Toast.makeText(activity, "蓝牙硬件不可用", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkBluetoothLocationSwitch(callback: () -> Unit) {
        if (LocationUtils.isLocationSwitchOpen(activity)) {
            // 有权限进入
            callback.invoke()
        } else {
            onEnterSettingPage?.onEnterPositionSetting()
        }
    }


    private fun checkBluetoothLocationPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            requestMultiplePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun checkBluetoothSwitch(callback: () -> Unit) {
        val service = activity.getSystemService(Context.BLUETOOTH_SERVICE)
        if (service is BluetoothManager &&
            service.adapter != null &&
            service.adapter.isEnabled
        ) {
            callback.invoke()
        } else {
            requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun checkBluetoothPermission(callback: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // must check and request bluetooth permission when targetSdk 31(VERSION_CODES.S)
            // before targetSdk 31(VERSION_CODES.S), bluetooth permission always enabled
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) {
                callback.invoke()
            } else {
                requestMultipleBPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                    )
                )
            }
        } else {
            callback.invoke()
        }
    }

    fun getAdapter(): BluetoothAdapter {
        val service = activity.getSystemService(Context.BLUETOOTH_SERVICE)
        if (service is BluetoothManager && service.adapter != null) {
            return service.adapter
        }
        throw IllegalStateException("cal fun checkFeature first")
    }

}