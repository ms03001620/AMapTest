package com.example.amaptest.stateless

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityStatelessBinding


class StatelessActivity : AppCompatActivity() {
    lateinit var binding: ActivityStatelessBinding

    private val viewModel by viewModels<StateViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatelessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatelessSwitch()
        initObserver()
    }

    private fun initObserver() {
        viewModel.checked.observe(this) {
            binding.statelessSwitch.setChecked(it, true)
        }
    }

    private fun setupStatelessSwitch() {
        binding.statelessSwitch.onCheckedChange = { userRequestedState ->
            Log.d("MainActivity", "state: $userRequestedState")

            viewModel.setCheckState(userRequestedState)
        }

        // Optional: Set text on the switch if you exposed the method
        binding.statelessSwitch.setText("Enable Feature")
    }
}