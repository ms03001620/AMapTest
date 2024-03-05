package polestar.base.ext

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.floor

inline fun ViewGroup.View(init: View.() -> Unit): View =
    View(context).apply(init).also { addView(it) }

inline fun Context.View(init: View.() -> Unit) =
    View(this).apply(init)

inline fun Context.LinearLayout(init: LinearLayout.() -> Unit): LinearLayout =
    LinearLayout(this).apply(init)

inline fun Context.TextView(init: TextView.() -> Unit) =
    TextView(this).apply(init)

inline var View.padding_top: Int
    get() {
        return 0
    }
    set(value) {
        setPadding(paddingLeft, value.dp, paddingRight, paddingBottom)
    }

inline var View.padding_bottom: Int
    get() {
        return 0
    }
    set(value) {
        setPadding(paddingLeft, paddingTop, paddingRight, value.dp)
    }

inline var View.padding_start: Int
    get() {
        return 0
    }
    set(value) {
        setPadding(value.dp, paddingTop, paddingRight, paddingBottom)
    }

inline var View.padding_end: Int
    get() {
        return 0
    }
    set(value) {
        setPadding(paddingLeft, paddingTop, value.dp, paddingBottom)
    }

inline var View.layout_width: Int
    get() {
        return 0
    }
    set(value) {
        val w = if (value > 0) value.dp else value
        val h = layoutParams?.height ?: 0
        layoutParams = ViewGroup.MarginLayoutParams(w, h)
    }

inline var View.layout_height: Int
    get() {
        return 0
    }
    set(value) {

        val w = layoutParams?.width ?: 0
        val h = if (value > 0) value.dp else value
        layoutParams = ViewGroup.MarginLayoutParams(w, h)
    }

inline var View.background_res: Int
    get() {
        return -1
    }
    set(value) {
        setBackgroundResource(value)
    }

inline var View.margin_top: Int
    get() {
        return -1
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = value.dp
        }
    }

inline var View.margin_bottom: Int
    get() {
        return -1
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            bottomMargin = value.dp
        }
    }

inline var TextView.textColor: String
    get() {
        return ""
    }
    set(value) {
        setTextColor(Color.parseColor(value))
    }

val match_parent = ViewGroup.LayoutParams.MATCH_PARENT
val wrap_content = ViewGroup.LayoutParams.WRAP_CONTENT

val visible = View.VISIBLE
val gone = View.GONE

val gravity_bottom = Gravity.BOTTOM

val Int.dp: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

val Int.sp: Int
    get() =
        if (this == 0) {
            0
        } else {
            floor(Resources.getSystem().displayMetrics.scaledDensity * this.toDouble()).toInt()
        }

val Float.dp: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

/**
 * 项目里面用的AutoSize, Resources.getSystem().displayMetrics是原始值,
 * 与修改过后的dpi不一致
 */
fun Context.dp(value: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value,
        resources.displayMetrics
    ).toInt()
}

fun View.dp(value: Int): Int {
    return context.dp(value.toFloat())
}

fun View.dp(value: Float): Int {
    return context.dp(value)
}
