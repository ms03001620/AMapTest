package com.robolectric

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R
import com.example.amaptest.marker.IconGenerator
import com.polestar.base.utils.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val iconCluster =
            IconGenerator(
                this,
                R.layout.charging_layout_marker_cluster_v3,
                Color.BLACK,
                offsetHeight4Text = 0
            )

        iconCluster.makeIcon("init")

        val button = findViewById<View>(R.id.login)
        button.setOnClickListener {
            //startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))

            GlobalScope.launch(Dispatchers.IO) {
                val start = System.currentTimeMillis()
                for(i in 0..100){
                    iconCluster.makeIcon(i.toString())
                }
                logd("pass:${System.currentTimeMillis()-start}", "IconGenerator")
            }

        }
    }
}