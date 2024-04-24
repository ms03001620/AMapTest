package com.polestar.base.views

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.ViewUtils
import com.polestar.base.R

/**
 * @desc 通用全局toast
 * 有需要自己添加
 */
object PolestarToast {

    /**
     * 显示Short Toast,默认居中位置
     *
     * @param context
     * @param message
     */
    fun showShortToast(message: String?, gravity: Int = Gravity.CENTER) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        var yOffset = 0
        if (gravity == Gravity.BOTTOM) {
            yOffset = SizeUtils.dp2px(32f)
        }
        ToastUtils.make()
            .setBgColor(ColorUtils.getColor(R.color.pc_black))
            .setTextColor(ColorUtils.getColor(R.color.pc_white))
            .setGravity(gravity, 0, yOffset)
            .show(message)
    }

    fun showShortToast(resId: Int) {
        showShortToast(StringUtils.getString(resId))
    }

    /**
     * 显示Long Toast
     *
     * @param context
     * @param message
     */
    fun showLongToast(
        message: String?,
        gravity: Int = Gravity.CENTER
    ) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        var yOffset = 0
        if (gravity == Gravity.BOTTOM) {
            yOffset = SizeUtils.dp2px(32f)
        }
        ToastUtils.make()
            .setBgColor(ColorUtils.getColor(R.color.pc_black))
            .setTextColor(ColorUtils.getColor(R.color.pc_white))
            .setGravity(gravity, 0, yOffset)
            .setDurationIsLong(true)
            .show(message)
    }

    fun showContinuousToast(
        message: String?,
        context: Context
    ) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        // show toast bellow
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
