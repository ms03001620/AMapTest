package com.example.amaptest.rect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R
import com.example.amaptest.databinding.ActivityRectBinding

class RectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityRectBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
    }
}

