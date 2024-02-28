package com.example.amaptest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.amaptest.databinding.ActivityLottieBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        initSoundBtn()
        initPointView()
    }

    private fun initPointView(){
        binding.pointView.setPoint(50)
        lifecycleScope.launch {
            for(i in 15 downTo 0){
                delay(500)
                binding.pointView.updateSeconds(i, 15)
            }
            binding.pointView.doneWithAnim(103600, 50, lifecycleScope)
        }
    }

    private fun initSoundBtn() {
        binding.btnSound.setMute(false)
        binding.btnSound.setOnClickListener {
            Toast.makeText(this, "mute:${binding.btnSound.isMute()}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}