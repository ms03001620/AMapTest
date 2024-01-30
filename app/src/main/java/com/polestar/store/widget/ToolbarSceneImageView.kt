package com.polestar.store.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.example.amaptest.R

/**
 * lwh created in 2023/11/22 15:56
 */
class ToolbarSceneImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var detectToX = 0
    private var detectToY = 0
    private var whiteThreshold = 0.8f
    private var blackThreshold = 0.2f
    private val liveData = MutableLiveData<AnalyseResult>()

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ToolbarSceneImageView
        ).let {
            detectToX = it.getDimensionPixelOffset(
                R.styleable.ToolbarSceneImageView_detectToX,
                0
            )
            detectToY = it.getDimensionPixelOffset(
                R.styleable.ToolbarSceneImageView_detectToY,
                0
            )
            whiteThreshold = it.getFloat(
                R.styleable.ToolbarSceneImageView_white_threshold,
                whiteThreshold
            )
            blackThreshold = it.getFloat(
                R.styleable.ToolbarSceneImageView_black_threshold,
                blackThreshold
            )
            it.recycle()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable == null) {
            analyseImageColor(null)
        } else if (drawable is BitmapDrawable) {
            analyseImageColor(drawable.bitmap)
        } else if (drawable is TransitionDrawable) {
            val realDrawable = runCatching {
                drawable.getDrawable(1) as? BitmapDrawable
            }.getOrNull()
            if (realDrawable != null) {
                analyseImageColor(realDrawable.bitmap)
            }
        }
    }

    private fun analyseImageColor(bitmap: Bitmap?) {
        if (bitmap == null) {
            liveData.value = AnalyseResult(true, ColorType.WHITE)
            return
        }
        val clipped = try {
            var clipWidth = Math.min(bitmap.width, detectToX)
            if (clipWidth == 0) {
                clipWidth = bitmap.width
            }
            var clipHeight = Math.min(bitmap.height, detectToY)
            if (clipHeight == 0) {
                clipHeight = bitmap.height
            }
            Bitmap.createBitmap(bitmap, 0, 0, clipWidth, clipHeight).convertNoAlphaBitmap()
        } catch (ex: Exception) {
            ex.printStackTrace()
            bitmap
        }
        Palette.from(clipped).clearFilters().addFilter { _, _ -> true }.generate {
            if (it == null) {
                liveData.value = AnalyseResult(false, ColorType.GENERAL)
                return@generate
            }
            val hslColor = it.dominantSwatch?.hsl
            if (hslColor == null) {
                liveData.value = AnalyseResult(false, ColorType.GENERAL)
                return@generate
            }
            if (hslColor[2] >= whiteThreshold) {
                // white color
                liveData.value = AnalyseResult(true, ColorType.WHITE)
            } else if (hslColor[2] <= blackThreshold) {
                // black color
                liveData.value = AnalyseResult(true, ColorType.BLACK)
            } else {
                liveData.value = AnalyseResult(true, ColorType.GENERAL)
            }
        }
    }

    private fun Bitmap.convertNoAlphaBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.RGB_565
        )
        val rect = Rect(0, 0, width, height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(this, rect, rect, null)
        return bitmap
    }

    fun getAnalyseLiveData(): LiveData<AnalyseResult> {
        return liveData
    }

    /**
     * 主色调分析结果
     * @param success 是否成功
     * @param colorType 颜色类型
     */
    class AnalyseResult(val success: Boolean, val colorType: ColorType)

    /**
     * 颜色倾向
     */
    enum class ColorType {
        WHITE,
        BLACK,
        GENERAL
    }
}
