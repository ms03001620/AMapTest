package com.example.amaptest.flow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityFlowAvdBinding
import kotlinx.coroutines.launch

class FlowAvdActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[FlowAvdViewModel::class.java]
    }

    lateinit var binding: ActivityFlowAvdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flow_avd)

        binding.btnSwitch.setOnClickListener {
            viewModel.createUuid()
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uuid.collect {
                    binding.textNumber.text = it.toString()
                }
            }
        }

    }


}