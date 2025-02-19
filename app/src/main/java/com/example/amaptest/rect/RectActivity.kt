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

        viewBinding.btnClear.setOnClickListener {
            viewBinding.gridView.deactivateAllCells()
        }

        viewBinding.btnGet.setOnClickListener {
            viewBinding.gridView.getActivatedCellsArray().let {
                println(it)
            }
        }
    }
}

