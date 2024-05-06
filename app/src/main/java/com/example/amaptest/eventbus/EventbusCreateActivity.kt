package com.example.amaptest.eventbus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R
import org.greenrobot.eventbus.EventBus

class EventbusCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eventbus_create)

        findViewById<View>(R.id.btnReceivePage).setOnClickListener {
            startActivity(Intent(this, ReceiveEventActivity::class.java))
        }

        findViewById<View>(R.id.btnEvent).setOnClickListener {
            EventBus.getDefault().postSticky("abc")
        }


    }

}