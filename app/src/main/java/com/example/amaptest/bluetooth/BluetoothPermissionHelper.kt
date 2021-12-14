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
    private val activity: AppCompatActivity
) {
    private var callback: (() -> Unit)? = null

    private var requestBluetoothLePermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                attemptGotoBluetoothLePage()
            } else {
                Toast.makeText(activity, "已取消蓝牙授权", Toast.LENGTH_LONG).show()
            }
        }

    private var requestBLEOnlyFinePermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                attemptGotoBluetoothLePage()
            } else {
                LocationUtils.goSettingForBluetooth(activity) {
                    Toast.makeText(activity, "已取消位置授权", Toast.LENGTH_LONG).show()
                }
            }
        }

    private var requestBluetooth =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                //granted
                attemptGotoBluetoothLePage()
            } else {
                //deny
                Toast.makeText(activity, "蓝牙已关闭", Toast.LENGTH_LONG).show()
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

    private fun checkFeature(callback: () -> Unit) {
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
            // 显示权限请求对话框
            LocationUtils.goSettingForBluetooth(activity) {
                Toast.makeText(activity, "无位置权限", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun checkBluetoothLocationPermission(callback: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            callback.invoke()
        } else {
            requestBLEOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
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
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED
            ) {
                callback.invoke()
            } else {
                requestBluetoothLePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        } else {
            callback.invoke()
        }
    }

}