package com.example.amaptest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.amaptest.marker.MarkerActionActivity


class AMapEnterActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amap_enter)


        // Location
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


        findViewById<View>(R.id.btn_cluster).setOnClickListener {
            gotoCluster()
        }

        findViewById<View>(R.id.btn_maker_action).setOnClickListener {
            gotoMarkerAction()
        }

        findViewById<View>(R.id.btn_map_performance).setOnClickListener {
            gotoMapPerformance()
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
        startActivity(Intent(this, MarkerActionActivity::class.java).also {
            it.putExtra("file_name", findViewById<EditText>(R.id.file_name).text.toString())
        })
    }

    fun gotoMapPerformance() {
        startActivity(Intent(this, MapPerformanceActivity::class.java))
    }

    fun gotoLocation() {
        startActivity(Intent(this, LocationActivity::class.java))
    }

    fun gotoCluster() {
        startActivity(Intent(this, ClusterActivity::class.java).also {
            it.putExtra("file_name", findViewById<EditText>(R.id.file_name).text.toString())
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}