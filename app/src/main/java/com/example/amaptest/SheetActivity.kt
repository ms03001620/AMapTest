package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.polestar.charging.utils.BottomMenuDialog
import com.polestar.charging.utils.ItemViewOnClick

// https://www.jianshu.com/p/1273effa2c55

// https://material.io/develop/android/components/bottom-sheet-dialog-fragment
class SheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)

        findViewById<View>(R.id.btn_dialog).setOnClickListener {
            showSheetDialog()
        }

        findViewById<View>(R.id.btn_show).setOnClickListener {
            val modalBottomSheet = StationDetailBottomSheet()
            modalBottomSheet.show(supportFragmentManager, TAG)
        }
    }

    fun showSheetDialog(){
        BottomMenuDialog.Builder(this).create(null, null)
    }


    companion object {
        const val TAG = "SheetActivity"
    }

}