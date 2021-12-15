package com.example.amaptest.bluetooth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.R
import com.example.amaptest.databinding.ActivityBluetoothSampleBinding

class BluetoothSampleActivity: AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_sample)
    }
}