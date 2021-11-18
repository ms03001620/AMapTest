package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

// https://www.jianshu.com/p/1273effa2c55

// https://material.io/develop/android/components/bottom-sheet-dialog-fragment
class SheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)


        findViewById<View>(R.id.btn_show).setOnClickListener {
            val modalBottomSheet = StationDetailBottomSheet()
            modalBottomSheet.show(supportFragmentManager, TAG)
        }
    }


    companion object {
        const val TAG = "SheetActivity"
    }

}