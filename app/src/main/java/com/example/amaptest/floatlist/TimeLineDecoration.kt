package com.example.amaptest.floatlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.polestar.base.R
import com.polestar.base.ext.dp

/**
 *
 * @Package: com.polestar.base.views.decoration
 * @ClassName: TimeLineDecoration
 * @Description: 时间轴
 * @Author:
 * @CreateDate: 2022/5/25 6:25 下午
 */
class TimeLineDecoration(context: Context) : RecyclerView.ItemDecoration() {

    // 当前轴点
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.base_color_FF7500)
    }

    // 画半透明大轴点
    private val mPaintAlpha: Paint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.base_orange_alpha_30)
    }

    // 画线和已完成轴点
    private val mPaint1: Paint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.base_orange_D5D6D8)
    }

    // 轴点半径
    private var circleRadius = 4.dp
    private val itemViewLeftOffset = 22.dp
    private val itemViewTopOffset = 24.dp

    private var circleToPaddingLeft = 10.dp

    // 圆圈相对item顶部的偏移量，约一行字的高度，圆要定位在第一行文字中部
    private val lineOffset = 10.dp

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(itemViewLeftOffset, itemViewTopOffset, 0, 0)

        //val position = parent.getChildAdapterPosition(view)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)


        parent.forEachIndexed { index, child ->
            if (child is ViewGroup) {
                // 绘制圆
                val centerX = 12.dp.toFloat()//顶部按钮半径； //child.left - circleToPaddingLeft.toFloat()
                val centerY = child.top * 1f + lineOffset // 圆圈相对item顶部的偏移量，约一行字的高度，圆要定位在第一行文字中部
                canvas.drawCircle(centerX, centerY, circleRadius.toFloat(), mPaint1)

                // 绘制上半部线
                val upLineTopY = (child.top - itemViewTopOffset).toFloat()
                val upLineBottomY = centerY - circleRadius// - circleRadius
                canvas.drawLine(centerX, upLineTopY, centerX, upLineBottomY, mPaint)

                val indexInAdapter = parent.getChildAdapterPosition(child)
                val size = parent.adapter?.itemCount ?: 0
                val isNotLast = indexInAdapter < size-1
                // 绘制下半部线
                if (isNotLast) {
                    val bottomLineTopY = centerY + circleRadius// + circleRadius
                    val bottomLineBottomY = child.bottom.toFloat()
                    canvas.drawLine(centerX, bottomLineTopY, centerX, bottomLineBottomY, mPaint1)
                }
            }
        }
    }
}
