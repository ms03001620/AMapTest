package com.example.amaptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityCarBinding
import com.polestar.base.views.PolestarToast

class CarActivity : AppCompatActivity() {
    lateinit var binding: ActivityCarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.ivSeat.getSteeringBtn().setOnClickListener {
            binding.ivSeat.enableSteering(true)
            PolestarToast.showShortToast("steeringView")
        }

        binding.ivSeat.getLevelLeftBtn().setOnClickListener {
            binding.ivSeat.setLeftSeatLevel(1)
            PolestarToast.showShortToast("levelLeft")
        }

        binding.ivSeat.getLevelRightBtn().setOnClickListener {
            binding.ivSeat.setRightSeatLevel(1)
            PolestarToast.showShortToast("levelRight")
        }

    }

}