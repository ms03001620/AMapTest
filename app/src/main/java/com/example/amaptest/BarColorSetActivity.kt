package com.example.amaptest

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.ActivityBarSetBinding


class BarColorSetActivity : AppCompatActivity() {
    lateinit var binding: ActivityBarSetBinding

    val tools = BarColor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bar_set)

        // statusbar icon color
        binding.btnWrite.setOnClickListener {
            tools.setDarkStatusIcon(this, false)
        }

        binding.btnBlack.setOnClickListener {
            tools.setDarkStatusIcon(this, true)
        }

        // statusbar bg color
        binding.btnBgRed.setOnClickListener {
            tools.setStatusBarColor(this, android.R.color.holo_red_dark)
        }

        binding.btnBgGreen.setOnClickListener {
            tools.setStatusBarColor(this, android.R.color.holo_green_dark)
        }

        binding.btnBgTrans.setOnClickListener {
            tools.setStatusBarColor(this, android.R.color.transparent)
        }

        //Immersion bar
        binding.checkImmersion.setOnCheckedChangeListener { buttonView, isChecked ->
            tools.setImmersionBar(this)
        }

        //FullScreen
        binding.checkFullScreen.setOnCheckedChangeListener { buttonView, isChecked ->
            tools.setFullScreen(this, isFull = isChecked)
        }

        //NaviBar
        binding.checkNaviBar.setOnCheckedChangeListener { buttonView, isChecked ->
            tools.setNavibar(this, hide = isChecked)
        }

        //Statusbar
        binding.checkStatusbar.setOnCheckedChangeListener { buttonView, isChecked ->
            tools.setStatusbar(this, hide = isChecked)
        }
    }


}