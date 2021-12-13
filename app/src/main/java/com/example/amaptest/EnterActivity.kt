package com.example.amaptest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.amaptest.ble.BleActivity
import com.example.amaptest.bluetooth.BluetoothActivity
import com.example.amaptest.flow.FlowActivity
import com.example.amaptest.marker.MarkerActionActivity

class EnterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        findViewById<View>(R.id.btn_enter).setOnClickListener {
            if (checkLocation()) {
                gotoLocation()
            } else {
                requestOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

        findViewById<View>(R.id.btn_map).setOnClickListener {
            if (checkLocation()) {
                gotoMap()
            } else {
                requestOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

        findViewById<View>(R.id.btn_anim).setOnClickListener {
            gotoAnim()
        }

        findViewById<View>(R.id.btn_bottomSheet).setOnClickListener {
            gotoSheet()
        }

        findViewById<View>(R.id.btn_sheetBehavior).setOnClickListener {
            gotoSheetBehavior()
        }

        findViewById<View>(R.id.btn_cluster).setOnClickListener {
            gotoCluster()
        }
        findViewById<View>(R.id.btn_flow).setOnClickListener {
            gotoFlow()
        }

        findViewById<View>(R.id.btn_maker_action).setOnClickListener {
            gotoMarkerAction()
        }

        // 蓝牙
        findViewById<View>(R.id.btn_bluetooth).setOnClickListener {
            //https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createBond()

            // android S 以上需要动态权限。否则
            // 调用 requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) 时
            // java.lang.SecurityException: Permission Denial: starting Intent { act=android.bluetooth.adapter.action.REQUEST_ENABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkBluetoothLocation()) {
                    gotoBluetooth()
                } else {
                    requestBluetoothPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                    )
                }
            } else {
                gotoBluetooth()
            }
        }

        //   requestOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        // 蓝牙LE
        findViewById<View>(R.id.btn_bluetooth_le).setOnClickListener {
            attemptGotoBluetoothLePage()
        }
    }

    fun attemptGotoBluetoothLePage() {
        checkFeature{
            checkBluetoothPermission {
                checkBluetoothSwitch{
                    checkBluetoothLocationPermission  {
                        checkBluetoothLocationSwitch{
                            gotoBluetoothLe()
                        }
                    }
                }
            }
        }
    }

    private fun checkBluetoothLocationSwitch(callback: () -> Unit) {
        if (LocationUtils.isLocationSwitchOpen(this)) {
            // 有权限进入
            callback.invoke()
        } else {
            // 显示权限请求对话框
            LocationUtils.goSettingForBluetooth(this) {
                Toast.makeText(this, "用户取消授权", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkBluetoothSwitch(callback: () -> Unit) {
        val service = getSystemService(Context.BLUETOOTH_SERVICE)
        if (service is BluetoothManager &&
            service.adapter != null &&
            service.adapter.isEnabled
        ) {
            callback.invoke()
        } else {
            requestBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }


    private fun checkFeature(callback: () -> Unit) {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        ) {
            callback.invoke()
        } else {
            Toast.makeText(this, "蓝牙硬件不可用", Toast.LENGTH_LONG).show()
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
                attemptGotoBluetoothLePage()
            } else {
                //deny
                Toast.makeText(this, "未打开开关", Toast.LENGTH_LONG).show()
            }
        }

    fun checkBluetoothLocationPermission(callback: () -> Unit) {
        if (checkLocation()) {
            callback.invoke()
        } else {
            requestBLEOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    fun checkBluetoothPermission(callback: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkBluetoothLocation()) {
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


    fun checkLocation(): Boolean {
        val t = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return t == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun checkBluetoothLocation(): Boolean {
        val p1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
        val p2 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
        return p1 && p2
    }

    fun gotoMap() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun gotoMarkerAction() {
        startActivity(Intent(this, MarkerActionActivity::class.java))
    }

    fun gotoAnim() {
        startActivity(Intent(this, AnimActivity::class.java))
    }

    fun gotoSheet() {
        startActivity(Intent(this, SheetActivity::class.java))
    }

    fun gotoSheetBehavior() {
        startActivity(Intent(this, SheetBehaviorActivity::class.java))
    }

    fun gotoLocation() {
        startActivity(Intent(this, LocationActivity::class.java))
    }

    fun gotoCluster() {
        startActivity(Intent(this, ClusterActivity::class.java))
    }

    fun gotoFlow() {
        startActivity(Intent(this, FlowActivity::class.java))
    }

    fun gotoBluetooth() {
        startActivity(Intent(this, BluetoothActivity::class.java))
    }

    fun gotoBluetoothLe() {
        startActivity(Intent(this, BleActivity::class.java))
    }



    private var requestBluetoothLePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                attemptGotoBluetoothLePage()
            } else {
                with(allGrants.keys.toString() + allGrants.values.toString()) {
                    Toast.makeText(applicationContext, "11111", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private var requestBLEOnlyFinePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                attemptGotoBluetoothLePage()
            } else {
                LocationUtils.goSettingForBluetooth(this) {
                    Toast.makeText(this, "用户取消授权", Toast.LENGTH_LONG).show()
                }
            }
        }



    private var requestOnlyFinePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                gotoLocation()
            } else {
                with(allGrants.keys.toString() + allGrants.values.toString()) {
                    Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
                }
            }
        }
    private var requestBluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { allGrants ->
            if (allGrants.values.all { it }) {
                gotoBluetooth()
            } else {
                with(allGrants.keys.toString() + allGrants.values.toString()) {
                    Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
                }
            }
        }


}