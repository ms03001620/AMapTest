package com.example.amaptest

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation


//https://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
class LocationActivity : AppCompatActivity() {
    lateinit var textInfo : TextView
    lateinit var helper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        textInfo = findViewById(R.id.text_location)

        findViewById<View>(R.id.btn_location).setOnClickListener {
            helper.onStart()
        }

        findViewById<View>(R.id.btn_location_stop).setOnClickListener {
            helper.onStop()
        }

        helper = LocationHelper(LocationImpl(applicationContext), object :LocationHelper.OnEvent{
            override fun onLoading() {
                addLogs("loading....")
            }

            override fun onResult(aMapLocation: AMapLocation) {
                addLogs(aMapLocation.toString())
            }

            override fun onError(aMapLocation: AMapLocation) {
                addLogs("error...." + aMapLocation.locationDetail)
            }
        })
    }

    fun addLogs(log: String){
        StringBuilder(textInfo.text.toString()).append("\n\n").append(log).let {
            textInfo.text = it.toString()
        }
    }
}