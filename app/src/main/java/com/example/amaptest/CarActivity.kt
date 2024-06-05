package com.example.amaptest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityCarBinding

class CarActivity : AppCompatActivity() {
    lateinit var binding: ActivityCarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

}