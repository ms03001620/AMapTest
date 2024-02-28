package com.polestar.store.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView
import com.example.amaptest.R

/**
 * 声音静音和解除静音
 *
 * 点击后先播放对应的lottie动画
 * 动画播放完毕后触发外部OnClick事件[outListener]
 */
class SoundButton(context: Context, attrs: AttributeSet?) : LottieAnimationView(context, attrs) {
    private var isMute = false
    private var outListener: OnClickListener? = null
    private var animCloseToOpen: Int = 0
    private var animOpenToClose: Int = 0

    init {
        installDefault()
    }

    private fun installDefault() {
        installAnim(R.raw.video_close_open, R.raw.video_open_close)
        speed = 0.5f
    }

    fun installAnim(animCloseToOpenRes: Int, animOpenToCloseRes: Int) {
        animCloseToOpen = animCloseToOpenRes
        animOpenToClose = animOpenToCloseRes
    }

    fun setMute(isMute: Boolean) {
        this.isMute = isMute
        if (isMute) {
            setAnimation(animCloseToOpen)
        } else {
            setAnimation(animOpenToClose)
        }
    }

    fun isMute() = isMute

    override fun setOnClickListener(l: OnClickListener?) {
        this.outListener = l
        super.setOnClickListener {
            if (!isAnimating) {
                if (!isMute) {
                    setAnimation(animOpenToClose)
                } else {
                    setAnimation(animCloseToOpen)
                }
                playAnimation()
            }
        }
    }


    private val animListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
            isMute = !isMute
            outListener?.onClick(this@SoundButton)
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addAnimatorListener(animListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAnimatorListener(animListener)
    }

}
