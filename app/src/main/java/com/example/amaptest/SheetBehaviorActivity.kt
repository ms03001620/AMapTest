package com.example.amaptest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SheetBehaviorActivity : AppCompatActivity() {
    private lateinit var behavior: BottomSheetBehavior<View>
    lateinit var targetView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet_behavior)

        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomSheet)
        targetView = findViewById(R.id.view)


        findViewById<View>(R.id.btn_show).setOnClickListener {
           if (behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
               behavior.isHideable= false
            } else {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
               behavior.isHideable= false
            }
        }

        findViewById<View>(R.id.btn_half).setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        findViewById<View>(R.id.btn_tran).setOnClickListener {

        }

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("SheetBehaviorActivity","onStateChanged:$newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("SheetBehaviorActivity","onSlide:$slideOffset")
            }
        }
        behavior.addBottomSheetCallback(bottomSheetCallback)
    }

}