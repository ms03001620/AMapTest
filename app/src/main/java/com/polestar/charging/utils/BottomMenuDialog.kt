package com.polestar.charging.utils

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.example.amaptest.R

class BottomMenuDialog(context: Context, themeId: Int) : Dialog(context, themeId) {

    class Builder(val context: Context) {
        fun create(list: List<String>?, listener: ItemViewOnClick?): BottomMenuDialog? {
            val dialog = BottomMenuDialog(context, R.style.BaseNoTitleDialogStyle)

            dialog.window?.run {
                setWindowAnimations(R.style.ChargingBottomDialogAnim)
                decorView.setPadding(0, 0, 0, 0)
                attributes.width = WindowManager.LayoutParams.MATCH_PARENT
                attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
                setGravity(Gravity.BOTTOM)
            }

            val view: View = LayoutInflater.from(context)
                .inflate(R.layout.charging_layout_bottom_menu, null)

/*            val recyclerView = view.findViewById<RecyclerView>(R.id.bottomItem)
            recyclerView.adapter = BottomMenuAdapter(dialog, list, listener)
            recyclerView.layoutManager = context.verticalLayoutManager()

            view.findViewById<TextView>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }*/

            dialog.run {
                setContentView(view)
                setCanceledOnTouchOutside(false)
                setCancelable(true)
                show()
            }
            return dialog
        }
    }
}