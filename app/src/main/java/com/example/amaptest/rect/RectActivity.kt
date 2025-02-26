package com.example.amaptest.rect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityRectBinding

class RectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityRectBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.privacyRectView.setDrawRectInfo(DrawRectInfo(
            max = 3,
            //data = MutableList<Shelter>(2){Shelter()}
            data = listOf(Shelter(
                hideAreaTopLeftX = 0,
                hideAreaTopLeftY = 0,
                hideAreaWidth = 100,
                hideAreaHeight = 100
            ))
        ))


        viewBinding.screenRectView.apply {
            targetWidth = 200
            targetHeight = 200
            strokeWidth = 4f
            setResultListener { x, y, w, h ->
                println("Result: x=$x, y=$y, w=$w, h=$h")
                // Do something with the result (e.g., update UI)
            }
        }

        viewBinding.screenRectView.setInitialRectList(listOf(0, 0, 100, 100))

        viewBinding.btnClear.setOnClickListener {
            viewBinding.gridView.deactivateAllCells()
            viewBinding.screenRectView.clear()
        }

        viewBinding.btnGet.setOnClickListener {
            viewBinding.screenRectView.setEnableDraw(true)
            viewBinding.gridView.getActivatedCellsArray().let {
                println(it)
            }
        }

        viewBinding.btnEdit.setOnClickListener {
            viewBinding.privacyRectView.enableEdit()
        }

        viewBinding.btnSave.setOnClickListener {
            viewBinding.privacyRectView.finishEdit()
            viewBinding.privacyRectView.getResult().let {
                println(it)
            }
        }
    }
}

