package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.example.amaptest.plate.Plate
import com.example.amaptest.plate.PlateInfo
import com.example.amaptest.plate.StationDetailBottomSheet
import com.polestar.charging.utils.BottomMenuDialog

class SheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)

        findViewById<View>(R.id.btn_dialog).setOnClickListener {
            showSheetDialog()
        }

        findViewById<View>(R.id.btn_show).setOnClickListener {
            val modalBottomSheet = StationDetailBottomSheet()

            mutableListOf(
                Plate("警AB12345", "vin1"),
                Plate("警AB12346", "vin2"),
         /*       Plate("警AB12347", "vin3"),*/
            ).let {
                PlateInfo("vin2", it)
            }.let {
                bundleOf(
                    StationDetailBottomSheet.EXTRA_DATA_ARGUMENTS to it
                )
            }.let {
                modalBottomSheet.arguments = it
            }

            modalBottomSheet.show(supportFragmentManager, TAG)
        }
    }

    fun showSheetDialog() {
        BottomMenuDialog.Builder(this).create(null, null)
    }


    companion object {
        const val TAG = "SheetActivity"
    }

}