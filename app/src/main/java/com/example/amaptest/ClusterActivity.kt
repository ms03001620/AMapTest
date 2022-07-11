package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.amaptest.ui.main.ClusterFragment

class ClusterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cluster_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ClusterFragment.newInstance().also {
                    intent.getStringExtra("file_name")?.let { fileName ->
                        it.arguments = Bundle().also {
                            it.putString("file_name", fileName)
                        }
                    }
                })
                .commitNow()
        }
    }
}