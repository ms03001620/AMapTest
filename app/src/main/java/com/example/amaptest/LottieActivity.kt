package com.example.amaptest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityLottieBinding

class LottieActivity : AppCompatActivity() {
    lateinit var binding: ActivityLottieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLottieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            binding.amin1.playAnimation()
            binding.amin2.playAnimation()
            binding.amin3.playAnimation()
        }

        binding.btnSound.setMute(false)

        binding.btnSound.setOnClickListener {
            Toast.makeText(this, "mute:${binding.btnSound.isMute()}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}