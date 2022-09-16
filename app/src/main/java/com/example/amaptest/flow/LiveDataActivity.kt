package com.example.amaptest.flow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityLivedataBinding

class LiveDataActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[LiveDataTestModel::class.java]
    }

    lateinit var binding: ActivityLivedataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_livedata)

        binding.btnSwitch.setOnClickListener {
            viewModel.setAny()
        }

        binding.btnClear.setOnClickListener {
            getViewModelStore().clear()
        }


        Log.d("LiveDataActivity", "observe")
        viewModel.data1.observe(this) {
            Log.d("LiveDataActivity", "data1:$it")
        }
        viewModel.data2.observe(this) {
            Log.d("LiveDataActivity", "data2:$it")
        }
        viewModel.data3.observe(this) {
            Log.d("LiveDataActivity", "data3:$it")
        }
        viewModel.data4.observe(this) {
            Log.d("LiveDataActivity", "data4:$it")
        }
    }




}