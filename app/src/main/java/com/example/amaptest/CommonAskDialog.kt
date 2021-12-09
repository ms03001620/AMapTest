package com.example.amaptest

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

/**
 * @author Mason
 * @date 2020-02-17
 */
class CommonAskDialog(context: Context, themeId: Int) : Dialog(context, themeId) {

    class Builder(
        val context: Context,
        val tvConfirm: String? = "",
        val tvCancel: String? = "",
        val titleString: String = "",
        val leftCallback: (() -> Unit)? = null
    ) {
        fun create(
            message: String,
            listener: View.OnClickListener,
            gravity: Int = Gravity.CENTER
        ): CommonAskDialog? {
            val dialog = CommonAskDialog(
                context,
                R.style.BaseNoTitleWithPaddingDialogStyle
            )
            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.base_ask_dialog_layout, null)
            view.findViewById<TextView>(R.id.tvCancel).setOnClickListener {
                dialog.dismiss()
                leftCallback?.invoke()
            }
            view.findViewById<TextView>(R.id.tvConfirm).setOnClickListener {
                listener.onClick(it)
                dialog.dismiss()
            }

            tvConfirm?.let {
                if(it.isNotEmpty()){
                    view.findViewById<TextView>(R.id.tvConfirm).text = tvConfirm
                }
            }

            tvCancel?.let {
                if(it.isNotEmpty()){
                    view.findViewById<TextView>(R.id.tvCancel).text = tvCancel
                }
            }

            if (titleString.isNotEmpty()) {
                view.findViewById<TextView>(R.id.tvTitle).text = titleString
                view.findViewById<TextView>(R.id.tvTitle).visibility = View.VISIBLE
            }
            view.findViewById<TextView>(R.id.tvMessage).text = message
            view.findViewById<TextView>(R.id.tvMessage).gravity = gravity
            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.show()
            return dialog
        }
    }
}