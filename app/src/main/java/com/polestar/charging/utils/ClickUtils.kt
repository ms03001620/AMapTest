package com.polestar.charging.utils

import android.widget.TextView

object ClickUtils {
    fun applySingleDebouncing(textPlateEdit: TextView, function: () -> Unit) {
        textPlateEdit.setOnClickListener{
            function()
        }



    }

}