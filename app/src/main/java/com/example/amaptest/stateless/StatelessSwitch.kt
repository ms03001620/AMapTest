package com.example.amaptest.stateless
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat

class StatelessSwitch(context: Context, attrs: AttributeSet?) : SwitchCompat(context, attrs) {

    var checkedState: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                super.setChecked(value)
                onCheckedChangeListener?.invoke(this, value)
            }
        }

    private var onCheckedChangeListener: ((SwitchCompat, Boolean) -> Unit)? = null

    init {


    }

    override fun setChecked(checked: Boolean) {
        // 不允许外部直接修改 checked 状态
    }


    @SuppressLint("MissingSuperCall")
    override fun jumpDrawablesToCurrentState() {


    }


}