package com.example.amaptest.rect

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityRectAreaBinding
import com.polestar.base.ext.dp

@SuppressLint("SetTextI18n")
class RectAreaActivity : AppCompatActivity() {
    val maxShapes = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = ActivityRectAreaBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.drawPathRectView.setData(
            data =  mutableListOf(
                mutableListOf<Point>(
                    Point(40, 0),
                    Point(40, 30),
                    Point(50, 50),
                    Point(70, 40),
                ),
                mutableListOf<Point>(
                    Point(0, 0),
                    Point(0, 30),
                    Point(30, 30),
                ),
                mutableListOf<Point>(),
                mutableListOf<Point>(),
            ),
            maxPointsPerShape = 6,
            maxShapes = maxShapes,
            scaleSize = Point(100, 100),
            callback = {
                Toast.makeText(this, "fffull", Toast.LENGTH_SHORT).show()
            },
            strokeWidth = 2.dp.toFloat()
        )


        viewBinding.btnClear.setOnClickListener {
            viewBinding.drawPathRectView.clear()
        }

        viewBinding.btnIndex.text = "[${viewBinding.drawPathRectView.currentShapeIndex + 1}]"
        viewBinding.btnIndex.setOnClickListener {
            var c = viewBinding.drawPathRectView.currentShapeIndex
            c++
            if (c == maxShapes) {
                c = 0
            }
            viewBinding.drawPathRectView.switchTo(c)
            viewBinding.btnIndex.text = "[${viewBinding.drawPathRectView.currentShapeIndex + 1}]"
        }

        viewBinding.btnEdit.setOnCheckedChangeListener { buttonView, isChecked ->
            viewBinding.drawPathRectView.enableEdit(isChecked)
            viewBinding.btnIndex.isEnabled = isChecked
        }


        viewBinding.btnSave.setOnClickListener {
            viewBinding.btnEdit.isChecked = false
            viewBinding.drawPathRectView.finishAppend()
        }
    }

}

