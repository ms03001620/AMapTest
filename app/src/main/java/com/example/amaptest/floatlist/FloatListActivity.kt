package com.example.amaptest.floatlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.amaptest.R
import com.polestar.customerservice.widget.floatlist.FloatListLayout
import com.polestar.customerservice.widget.floatlist.RepairStep

class FloatListActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_float_list)

        findViewById<View>(R.id.btnTitle).setOnClickListener {
            Toast.makeText(this, "oh!!!", Toast.LENGTH_SHORT).show()
        }

        val floatListLayout = findViewById<FloatListLayout>(R.id.floatingView)

        floatListLayout.setTitle("预约中")
        floatListLayout.setData(mockList)
    }


    companion object {
        val mockList = (1..10).map {
            RepairStep(id = it, "待支付", "维修已完成，请支付订单", "10-03 18:%2d".format(it))
        }
    }
}
