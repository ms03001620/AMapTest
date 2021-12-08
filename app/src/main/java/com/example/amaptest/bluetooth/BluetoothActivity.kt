package com.example.amaptest.bluetooth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityAnimBinding
import com.example.amaptest.databinding.ActivityBluetoothBinding
import com.example.amaptest.databinding.ActivityFlowBinding

class BluetoothActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[BluetoothViewModel::class.java]
    }

    lateinit var binding: ActivityBluetoothBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth)

        initBtns()
        initObserve()
        initStart()
    }

    private fun initBtns() {

    }

    private fun initObserve() {

    }

    private fun initStart() {

    }
}