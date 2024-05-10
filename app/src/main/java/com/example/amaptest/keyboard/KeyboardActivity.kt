package com.example.amaptest.keyboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Range
import android.widget.EditText
import com.example.amaptest.R
import com.polestar.base.ext.RangeFilter
import com.polestar.base.ext.RangeInputFilter
import com.polestar.base.ext.digit
import com.polestar.base.ext.setupFilter
import com.polestar.base.views.PolestarToast

/**
 * https://blog.csdn.net/ShuSheng0007/article/details/104232176
 */
class KeyboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard)

        val edit = findViewById<EditText>(R.id.et_input2)

/*        edit.addTextChangedListener(RangeTextWatcher(
            range = Range(0, 100),
            callback = object : RangeTextWatcher.OnWatcherCallback {
                override fun onOutRange(input: Int?, range: Range<Int>) {
                    val errorInfo = if (range.lower <= 0 && range.upper <= 0) {
                        "已超出可用积分"
                    } else {
                        "请输入%1s -%2s".format(range.lower.toString(), range.upper.toString())
                    }
                    PolestarToast.showShortToast(errorInfo)
                }

                override fun onInRange(input: Int, range: Range<Int>) {
                    PolestarToast.showShortToast("ok:$input")
                }
            }
        ))*/

        //edit.setupFilter(digit)

        edit.setupFilter(digit, RangeInputFilter(0, 5000,{
            edit.setText(it)
            edit.setSelection(it.length)

        },{
            PolestarToast.showShortToast("out range")
        }))
    }
}

