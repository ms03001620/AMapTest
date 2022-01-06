package com.example.amaptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import com.polestar.charging.ui.station.plate.Plate
import com.polestar.charging.ui.station.plate.PlateSelectorBottomSheet

class SheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheet)

        // normal
        findViewById<View>(R.id.btn_show).setOnClickListener {
            mutableListOf(
                Plate("警AB12345", "LYVPKBDTDLB000080"),
                Plate("警AB1234", "LYVPKBDTDLB000081"),
                /*       Plate("警AB12347", "vin3"),*/
            ).let {
                showDialog(it)
            }
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
                    showDialog(it)
                }
            }
        }

        // empty
        findViewById<View>(R.id.btn_show_empty).setOnClickListener {
            showDialog(emptyList())
        }
    }

    private fun showDialog(plates: List<Plate>) {
        bundleOf(
            PlateSelectorBottomSheet.EXTRA_DATA_ARGUMENTS to plates
        ).let {
            val modalBottomSheet = PlateSelectorBottomSheet()
            modalBottomSheet.arguments = it
            modalBottomSheet.show(supportFragmentManager, TAG)
        }
    }


    companion object {
        const val TAG = "SheetActivity"
    }

}