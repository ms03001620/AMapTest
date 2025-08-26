package com.example.amaptest.rect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.amaptest.databinding.ActivitySeatBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Arrays

class SeatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivitySeatBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        lifecycleScope.launch {
            delay(1000)

            viewBinding.seatView.setData(area, seats.map { it.copy() })
            //viewBinding.seatViewNoScroll.setData(area, seats)
        }
    }

    val area = SeatArea(
        areaWidth = 300,
        areaHeight = 500,
    )

    val seats = listOf(
        SeatData(x = 0f, y = 0f, width = 100f, height = 100f, seatStatus = SeatStatus.Disable),
        SeatData(x = 100f, y = 400f, width = 100f, height = 100f, seatStatus = SeatStatus.UnChecked),
        SeatData(x = 200f, y = 0f, width = 100f, height = 100f, seatStatus = SeatStatus.Checked),
        SeatData(x = 200f, y = 300f, width = 100f, height = 200f, seatStatus = SeatStatus.Checked),
        SeatData(x = 30f, y = 200f, width = 197f, height = 140f, seatStatus = SeatStatus.UnChecked),
    )

}


