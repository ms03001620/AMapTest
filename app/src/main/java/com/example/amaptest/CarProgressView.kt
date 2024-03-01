package com.example.amaptest

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils

class CarProgressView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val translateAnimation =
        AnimationUtils.loadAnimation(context, R.anim.charging_anim_car)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation(translateAnimation)
    }

    override fun onDetachedFromWindow() {
        clearAnimation()
        super.onDetachedFromWindow()
    }
}