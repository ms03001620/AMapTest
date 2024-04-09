package com.polestar.customerservice.widget.floatlist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.RecyclerView
import com.polestar.base.ext.dp

/**
 *
 * @Package: com.polestar.base.views.decoration
 * @ClassName: TimeLineDecoration
 * @Description: 时间轴
 * @Author:
 * @CreateDate: 2022/5/25 6:25 下午
 */
class TimeLineDecoration(
    private val lineColor: Int = Color.parseColor("#D6D7D9"),
    private val circleRadius: Int = 4.dp,
    private val itemViewLeftOffset: Int = 22.dp,
    private val itemViewBottomOffset: Int = 24.dp,
    private val itemViewTopOffset: Int = 6.dp,
    private val lineOffset: Int = 10.dp, //圆圈相对item顶部的偏移量，约一行字的高度，圆要定位在第一行文字中部
) : RecyclerView.ItemDecoration() {

    private val paintGray: Paint = Paint().apply {
        isAntiAlias = true
        color = lineColor
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(itemViewLeftOffset, itemViewTopOffset, 0, itemViewBottomOffset)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        val itemCount = parent.adapter?.itemCount ?: 0

        parent.forEachIndexed { _, child ->
            if (child is ViewGroup) {
                val indexInAdapter = parent.getChildAdapterPosition(child)
                val isNotFirst = indexInAdapter != 0

                val centerX = 12.dp.toFloat()//顶部按钮半径
                val centerY = child.top + lineOffset.toFloat()

                // 绘制上半部线
                val upLineTopY = (child.top).toFloat() - itemViewTopOffset.toFloat()
                val upLineBottomY = centerY
                canvas.drawLine(centerX, upLineTopY, centerX, upLineBottomY, paintGray)

                val isNotLast = indexInAdapter < itemCount - 1
                // 绘制下半部线
                if (isNotLast) {
                    val bottomLineTopY = centerY
                    val bottomLineBottomY = child.bottom.toFloat() + itemViewBottomOffset
                    canvas.drawLine(centerX, bottomLineTopY, centerX, bottomLineBottomY, paintGray)
                }

                if (isNotFirst) {
                    // 绘制圆
                    canvas.drawCircle(centerX, centerY, circleRadius.toFloat(), paintGray)
                }
            }
        }
    }
}
