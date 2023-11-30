package com.example.amaptest

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class BarColor {


    fun setDarkStatusIcon(activity: Activity, bDark: Boolean) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val decorView = activity.window.decorView
        var vis = decorView.systemUiVisibility
        vis = if (bDark) {
            vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        decorView.systemUiVisibility = vis
    }

    fun setStatusBarColor(activity: Activity, @ColorRes colorRes: Int) {
        activity.window.statusBarColor = ContextCompat.getColor(activity, colorRes)
    }



    fun setImmersionBar(
        activity: Activity,
        @ColorInt color: Int,
        hideStatusBar: Boolean,
        isDark: Boolean
    ) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
/*        if (isDark) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }*/

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE


        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //window.statusBarColor = color
        val winParams = window.attributes
        if (hideStatusBar) {
            winParams.flags =
                winParams.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        } else {
            winParams.flags =
                winParams.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        }
        window.attributes = winParams
    }

    fun setFullScreen(activity: Activity, isFull: Boolean) {
        //https://developer.android.com/develop/ui/views/layout/immersive?hl=zh-cn
        val window  = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (isFull) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }


    fun setNavibar(activity: Activity, hide: Boolean) {
        //https://developer.android.com/develop/ui/views/layout/immersive?hl=zh-cn
        val window  = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (hide) {
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    fun setStatusbar(activity: Activity, hide: Boolean) {
        //https://developer.android.com/develop/ui/views/layout/immersive?hl=zh-cn
        val window  = activity.window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (hide) {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        }
    }



}