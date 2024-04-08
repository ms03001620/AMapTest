package com.example.amaptest.floatlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.amaptest.R
import com.polestar.customerservice.widget.FloatLayout

class FloatListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_float_list)

        findViewById<View>(R.id.btnTitle).setOnClickListener {
            Toast.makeText(this, "oh!!!", Toast.LENGTH_SHORT).show()
        }

        val floatLayout = findViewById<FloatLayout>(R.id.floatingView)

        floatLayout.setData(mockList)
    }


    companion object {
        val mockList = (1..10).map {
            RepairStep(id = it, "待支付", "维修已完成，请支付订单", "10-03 18:%2d".format(it))
        }
    }
}

data class RepairStep(val id: Int, val title: String, val subTitle: String, val time: String)