package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityAuxiliaryLineBinding

@SuppressLint("SetTextI18n")
class AuxiliaryLineActivity : AppCompatActivity() {
    val maxShapes = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityAuxiliaryLineBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.auxiliaryLineView.setGraphNumber(4)

        viewBinding.auxiliaryLineView.setCurrentGraph(
            counterPoint.filterIndexed { index, _ ->
                index != configIndex && index != osdIndex
            }
        )

        viewBinding.btnClear.setOnClickListener {

        }


        viewBinding.btnIndex.setOnClickListener {

        }

        viewBinding.btnEdit.setOnCheckedChangeListener { buttonView, isChecked ->

        }

        viewBinding.btnSwitch.setOnClickListener {
            viewBinding.auxiliaryLineView.swapAB()
        }


        viewBinding.btnSave.setOnClickListener {
            val msg = viewBinding.auxiliaryLineView.getCurrentGraph().toTypedArray().contentToString()
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

        }
    }

    companion object {
        const val configIndex = 0
        const val aPointIndex = 1
        const val bPointIndex = 2
        const val osdIndex = 3
        const val startPointIndex = 4
        const val endPointIndex = 5

        val counterPoint: MutableList<MutableList<Int>> = mutableListOf(
            mutableListOf(2, 20),//0 config
            mutableListOf(167, 167),//1 a point
            mutableListOf(338, 327),//2 b point
            mutableListOf(143, 387),// osd same to index 4
            mutableListOf(143, 387),//4 start
            mutableListOf(363, 108)//5 end
        )

        val groupGraph = mutableListOf(
            counterPoint,
            mutableListOf(
                mutableListOf(2, 20),
                mutableListOf(532, 306),
                mutableListOf(703, 327),
                mutableListOf(629, 520),
                mutableListOf(629, 520),
                mutableListOf(662, 115)
            )
        )

        val counterPointWithPath: MutableList<MutableList<Int>> = mutableListOf(
            mutableListOf(10, 20),
            mutableListOf(359, 221),
            mutableListOf(561, 324),
            mutableListOf(33, 340),
            mutableListOf(33, 340),
            mutableListOf(195, 202),
            mutableListOf(192, 209),
            mutableListOf(394, 424),
            mutableListOf(526, 121),
            mutableListOf(646, 380),
            mutableListOf(428, 530),
            mutableListOf(171, 492),
            mutableListOf(141, 334),
            mutableListOf(81, 129)
        )

    }

}

