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

        viewBinding.btnClear.setOnClickListener {

        }


        viewBinding.btnIndex.setOnClickListener {

        }

        viewBinding.btnEdit.setOnCheckedChangeListener { buttonView, isChecked ->

        }


        viewBinding.btnSave.setOnClickListener {

        }
    }

}

