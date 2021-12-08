package com.example.amaptest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

        findViewById<View>(R.id.btn_bluetooth).setOnClickListener {
            gotoBluetooth()
        }
    }

    fun checkLocation(): Boolean {
        val t = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return t == PackageManager.PERMISSION_GRANTED
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

    fun gotoSheetBehavior(){
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
}