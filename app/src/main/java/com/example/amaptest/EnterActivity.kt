package com.example.amaptest

import android.Manifest
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