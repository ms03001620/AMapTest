package com.example.amaptest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.amaptest.databinding.ActivityAnimBinding


class AnimActivity : AppCompatActivity() {
    lateinit var floatActionHelper: FloatActionHelper
    lateinit var binding: ActivityAnimBinding

    var isCharging = false
    var isMapPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_anim)
        floatActionHelper = FloatActionHelper(binding.contentFabs, resources)

        binding.layoutMain.textHello1.text = "abc"

        binding.layoutMain.btnCharging.setOnClickListener {
            isCharging = isCharging.not()
            floatActionHelper.setToCharging(isCharging)
        }

        binding.layoutMain.btnPageType.setOnClickListener {
            isMapPage = isMapPage.not()
            floatActionHelper.setMapMode(isMapPage)
        }

        floatActionHelper.setOnClickListener {
            val string = when (it.id) {
                R.id.fab_base -> "fab_base"
                R.id.fab_favorite -> "fab_favorite"
                R.id.fab_help -> "fab_help"
                R.id.fab_positioning -> "fab_positioning"
                R.id.fab_orders -> "fab_orders"
                else -> "id$it.id"
            }
            Toast.makeText(applicationContext, string, Toast.LENGTH_SHORT).show()
        }
    }

}