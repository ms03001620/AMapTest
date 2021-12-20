package com.example.amaptest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.amaptest.ble.BleActivity
import com.example.amaptest.bluetooth.BluetoothActivity
import com.example.amaptest.bluetooth.BluetoothPermissionHelper
import com.example.amaptest.bluetooth.BluetoothSampleActivity
import com.example.amaptest.flow.FlowActivity
import com.example.amaptest.marker.MarkerActionActivity

class EnterActivity : AppCompatActivity() {
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

    private val helper = BluetoothPermissionHelper(this, object : BluetoothPermissionHelper.OnEnterSettingPage {
        override fun onEnterPositionSetting() {
            showAlertDialog(
                getActivity(),
                R.string.cs_permission_prompt_location,
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
                R.string.cs_permission_prompt_bluetooth,
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
            helper.attemptRunCallback {
                gotoBluetooth()
            }
        }

        //   requestOnlyFinePermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        // 蓝牙LE
        findViewById<View>(R.id.btn_bluetooth_le).setOnClickListener {
            //attemptGotoBluetoothLePage()
            helper.attemptRunCallback {
                gotoBluetoothLe()
            }
        }

        findViewById<View>(R.id.btn_bluetooth_sample).setOnClickListener {
            //attemptGotoBluetoothLePage()
            helper.attemptRunCallback {
                gotoBluetoothSample()
            }
        }
    }

    private fun checkLocation() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun getActivity() = this

    private fun showAlertDialog(
        context: Context,
        messageResId: Int,
        leftCallback: (() -> Unit)? = null,
        rightCallback: (() -> Unit)? = null
    ) {
        CommonAskDialog.Builder(
            context,
            context.getString(R.string.charging_open_settings),
            context.getString(R.string.base_cancel),
            leftCallback = leftCallback
        ).create(
            context.getString(messageResId), listener = { rightCallback?.invoke() })
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

    fun gotoBluetoothSample() {
        startActivity(Intent(this, BluetoothSampleActivity::class.java))
    }

}