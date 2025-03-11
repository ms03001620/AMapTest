package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityAuxiliaryLineBinding
import com.example.amaptest.databinding.ActivityRectAreaBinding
import com.polestar.base.ext.dp

@SuppressLint("SetTextI18n")
class AuxiliaryLineActivity : AppCompatActivity() {
    val maxShapes = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityAuxiliaryLineBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.auxiliaryLineView.setData(counterPoint)

        viewBinding.btnClear.setOnClickListener {

        }


        viewBinding.btnIndex.setOnClickListener {

        }

        viewBinding.btnEdit.setOnCheckedChangeListener { buttonView, isChecked ->

        }

        viewBinding.btnSwitch.setOnClickListener {
            Toast.makeText(this, "switch", Toast.LENGTH_SHORT).show()
            viewBinding.auxiliaryLineView.swapAB()
        }


        viewBinding.btnSave.setOnClickListener {

        }
    }

    companion object {
        val counterPoint: MutableList<MutableList<Int>> = mutableListOf(
            mutableListOf(2, 20),
            mutableListOf(167, 167),//1 a point
            mutableListOf(338, 327),//2 b point
            mutableListOf(143, 387),
            mutableListOf(143, 387),//4 start
            mutableListOf(363, 108)//5 end
        )
    }

}

