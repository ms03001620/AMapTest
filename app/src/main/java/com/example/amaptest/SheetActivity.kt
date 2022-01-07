package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import com.polestar.charging.ui.station.plate.Plate
import com.polestar.charging.ui.station.plate.PlateSelectorBottomSheet
import com.polestar.charging.ui.station.plate.VehiclesViewModel

class SheetActivity : AppCompatActivity() {
    private val vehiclesViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[VehiclesViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)

        vehiclesViewModel.getVehicles()
        // normal
        findViewById<View>(R.id.btn_show).setOnClickListener {
            mutableListOf(
                Plate("警AB12345", "LYVPKBDTDLB000080"),
                Plate("警AB1234", "LYVPKBDTDLB000081"),
                /*       Plate("警AB12347", "vin3"),*/
            ).let {
                vehiclesViewModel.setMockData(it)
            }
            showDialog()
        }

        // byNubmer
        findViewById<View>(R.id.btn_show_number).setOnClickListener {
            findViewById<EditText>(R.id.edit_number).let {
                it.text.toString().toInt()
            }.let { count ->
                mutableListOf<Plate>().also { list ->
                    for (i in 1..count) {
                        list.add(Plate("警AB1234" + i, "LYVPKBDTDLB00008" + i))
                    }
                }.let {
                    vehiclesViewModel.setMockData(it)
                }
                showDialog()
            }
        }

        // empty
        findViewById<View>(R.id.btn_show_empty).setOnClickListener {
            vehiclesViewModel.setMockData(emptyList())
            showDialog()
        }
    }

    private fun showDialog() {
        PlateSelectorBottomSheet().show(supportFragmentManager, TAG)
    }

    companion object {
        const val TAG = "SheetActivity"
    }
}