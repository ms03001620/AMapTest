package com.example.amaptest

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import java.lang.IllegalArgumentException
import java.lang.ref.WeakReference

class TopSheetBehavior<V : View> @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
) : CoordinatorLayout.Behavior<V>(context, attrs) {
    private var mViewDragHelper: ViewDragHelper? = null
    private var mViewRef: WeakReference<V>? = null
    private var mMinOffset = 0
    private var mMaxOffset = 0
    private val outsideColor = -0x66efe7e0
    private var parent: CoordinatorLayout? = null
    private val outside = ColorDrawable()
    private var state = State.STATE_COLLAPSED
    private var stateCallback: ((State) -> Unit)? = null

    enum class State {
        STATE_EXPANDED,
        STATE_COLLAPSED
    }

    private val mDragCallback = object : ViewDragHelper.Callback() {
        private var visible = false
        var collapsedCallback = {}
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return mViewRef != null && mViewRef!!.get() === child
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int,
        ) {
            val childHeight = changedView.height
            val boundsTop = childHeight + top
            val offset = boundsTop.toFloat() / childHeight
            outside.setBounds(0, boundsTop, parent!!.width, parent!!.height)
            val baseAlpha = (outsideColor and -0x1000000 ushr 24)
            val grayScale = (baseAlpha * offset).toInt()
            val color = grayScale shl 24 or (outsideColor and 0xffffff)
            outside.color = color
            val visible = offset != 0f
            changedView.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            if (this.visible != visible) {
                if (visible) {
                    parent!!.overlay.add(outside)
                    state = State.STATE_EXPANDED
                } else {
                    parent!!.overlay.remove(outside)
                    state = State.STATE_COLLAPSED
                    collapsedCallback()
                    collapsedCallback = {}
                }
                this.visible = visible
                stateCallback?.let { it(state) }
                parent!!.invalidate()
            }
        }
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        this.parent = parent
        parent.onLayoutChild(child, layoutDirection)
        mMinOffset = -child.height
        mMaxOffset = 0
        if (state == State.STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset)
        } else if (state == State.STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset)
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
            mViewDragHelper?.setEdgeTrackingEnabled(ViewDragHelper.EDGE_TOP)
        }
        mViewRef = WeakReference(child)
        return true
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, ev: MotionEvent): Boolean {
        val rect = Rect()
        child.getLocalVisibleRect(rect)
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_DOWN) {
            if (ev.x < rect.left || ev.x > rect.right || ev.y < rect.top || ev.y > rect.bottom) {
                setState(State.STATE_COLLAPSED)
            }
        }
        return super.onTouchEvent(parent, child, ev)
    }

    fun getState(): State {
        return state
    }

    fun setState(newState: State) {
        mDragCallback.collapsedCallback = {}
        if (newState == state) {
            return
        }
        val child: V = mViewRef?.get() ?: return
        val top: Int = when (newState) {
            State.STATE_COLLAPSED -> {
                mMinOffset
            }
            State.STATE_EXPANDED -> {
                mMaxOffset
            }
            else -> {
                throw IllegalArgumentException("Illegal state argument: $newState")
            }
        }
        if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
            ViewCompat.postOnAnimation(child, SettleRunnable(child))
        }
    }

    fun setCollapsedCallback(collapsedCallback: () -> Unit) {
        mDragCallback.collapsedCallback = collapsedCallback
        if (state == State.STATE_COLLAPSED) {
            collapsedCallback()
            return
        }
        val child: V = mViewRef?.get() ?: return
        if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, mMinOffset)) {
            ViewCompat.postOnAnimation(child, SettleRunnable(child))
        }
    }

    fun setStateCallback(callback: (State) -> Unit) {
        stateCallback = callback
    }

    companion object {
        fun <V : View> from(view: V): TopSheetBehavior<V> {
            val params = view.layoutParams
            require(params is CoordinatorLayout.LayoutParams) { "The view is not a child of CoordinatorLayout" }
            val behavior = params.behavior
            require(behavior is TopSheetBehavior) { "The view is not associated with TopSheetBehavior" }
            return behavior as TopSheetBehavior<V>
        }
    }

    inner class SettleRunnable internal constructor(
        private val mView: View,
    ) :
        Runnable {
        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this)
            }
        }
    }
}