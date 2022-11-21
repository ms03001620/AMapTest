package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.amaptest.pager.ResendTextView

class CountdownActivity : AppCompatActivity() {
    lateinit var countDown: ResendTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        countDown = findViewById<ResendTextView>(R.id.countdown)
        countDown.runStoreTime(object: ResendTextView.OnRestoreTime{
            override fun onNoStoreData() {
                Toast.makeText(getActivity(), "NoData", Toast.LENGTH_LONG).show()
            }
        })

        findViewById<View>(R.id.btn_start).setOnClickListener {
            countDown.init(20)
        }

        findViewById<View>(R.id.btn_clear).setOnClickListener {
            countDown.clear()
        }

        countDown.setOnClickListener {
            countDown.init(20)
        }
    }

    fun getActivity() = this
}