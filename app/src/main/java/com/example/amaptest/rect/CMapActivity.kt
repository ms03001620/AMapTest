package com.example.amaptest.rect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.amaptest.databinding.ActivityCmapBinding
import jp.linktivity.citypass.temp.one.Seat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CMapActivity : AppCompatActivity() {
    lateinit var binding: ActivityCmapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCmapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(1000)
        }
        init()
    }

    private fun init() {
        // 模拟数据
        val seats = mutableListOf<Seat>()
        var id = 0
        for (row in 0 until 10) {
            for (col in 0 until 15) {
                val width = 80f + (row % 3) * 10 // 随机不同大小
                val height = 80f
                seats.add(
                    Seat(
                        id = "S$id",
                        x = col * 100f,
                        y = row * 100f,
                        width = width,
                        height = height
                    )
                )
                id++
            }
        }
        binding.seatMapView.setSeats(seats)
    }

}


