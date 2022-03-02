package com.robolectric

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.amaptest.R

class DialogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialogs)

        findViewById<View>(R.id.btn).setOnClickListener {
            PolicyFragmentDialog.newInstance("hello2")
                .show(supportFragmentManager, "FRAGMENT_TAG_POLICY")
            PolicyFragmentDialog.newInstance("hello1")
                .show(supportFragmentManager, "FRAGMENT_TAG_POLICY")
        }
    }
}