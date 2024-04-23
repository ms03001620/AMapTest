package com.example.amaptest

import android.os.Bundle
import android.widget.SeekBar
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
        initMask()
    }

    private fun initMask(){
        binding.seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.maskCar.process(progress / 100f)
                binding.maskCarRing.process(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        binding.maskCar.setProcessTextEnable(true)
        binding.maskCarRing.setProcessTextEnable(true)

        binding.checkCharging.setOnCheckedChangeListener { _, isChecked ->
            binding.maskCar.setCharging(isChecked)
            binding.maskCarRing.setCharging(isChecked)
        }

        binding.checkText.setOnCheckedChangeListener { _, isChecked ->
            binding.maskCar.setProcessTextEnable(isChecked)
            binding.maskCarRing.setProcessTextEnable(isChecked)
        }
    }

    private fun initPointView(){
        val point = 100

        binding.pointView.setPoint(point)
        lifecycleScope.launch {
            for(i in 15 downTo 0){
                delay(150)
                binding.pointView.updateSeconds(i, 15)
            }
            binding.pointView.doneWithAnim(103600, point, lifecycleScope)
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