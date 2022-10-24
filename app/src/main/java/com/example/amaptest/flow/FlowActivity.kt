package com.example.amaptest.flow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.databinding.ActivityAnimBinding
import com.example.amaptest.databinding.ActivityFlowBinding

class FlowActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[FlowViewModel::class.java]
    }

    lateinit var binding: ActivityFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_flow)

        initBtns()
        initObserve()
        //initStart()

        viewModel.linkToList()
    }

    private fun initBtns() {
        binding.btnSwitch.setOnClickListener {
            viewModel.getNewsOdd()
        }
        binding.btnAddItem.setOnClickListener {
            viewModel.addItems(System.currentTimeMillis().toString())
        }
    }

    private fun initObserve() {
        viewModel.news.observe(this) {
            binding.textNumber.text = it.toString()
        }
        viewModel.itemString.observe(this) {
            binding.textItems.text = it
        }

    }

    private fun initStart() {
        viewModel.getNews()

    }
}