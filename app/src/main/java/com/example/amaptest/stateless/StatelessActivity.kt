package com.example.amaptest.stateless

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityStatelessBinding
import com.example.amaptest.stateless.KeepViewModel.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class StatelessActivity : AppCompatActivity() {
    lateinit var binding: ActivityStatelessBinding
    private val viewModel by viewModels<StateViewModel>()
    private val keepViewModel= KeepViewModel(CoroutineScope(SupervisorJob()))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatelessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStatelessSwitch()
        initCheckUpdateViewModel()
        initObserver()

        keepViewModel.postRequest(Request("1") {
            println("resp:$it")
        })
    }

    private fun initObserver() {
        viewModel.checked.observe(this) {
            binding.statelessSwitch.setChecked(it, true)
        }
    }

    private fun setupStatelessSwitch() {
        binding.statelessSwitch.onCheckedChange = { userRequestedState ->
            println("state: $userRequestedState")
            viewModel.setCheckState(userRequestedState)
        }
    }

    private fun initCheckUpdateViewModel(){
        binding.checkUpdate.setOnCheckedChangeListener{ _, isChecked ->
            viewModel.enableUpdateViewModel(isChecked)
        }
    }
}