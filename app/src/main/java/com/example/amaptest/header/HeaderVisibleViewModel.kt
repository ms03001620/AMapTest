package com.example.amaptest.header

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.polestar.base.utils.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.max

/**
 * 计算列表header可见度
 *
 * 默认list中存在一个header并且在列表第一项
 * 根据header滚动Y坐标判断header是否滚出屏幕
 * 转化为可见到不可见 1.0 -> 0.0
 */
class HeaderVisibleViewModel : ViewModel() {
    val headerVisibleLiveData = MutableLiveData<Float>()

    private fun visibleToAlpha(top: Int, height: Int) =
        min(1.0f, (abs(top).toFloat() / height))

    /**
     * 将header可见度转化为0到1之间
     */
    private fun normalizeHeaderVisible(layoutManager: LinearLayoutManager): Float {
        val vp = layoutManager.findFirstVisibleItemPosition()
        val headerIndex = 0// 默认header是列表中第一项
        var visible = 1.0f//可见度默认可见

        if (vp == headerIndex) {
            val headerView = layoutManager.getChildAt(headerIndex)
            if (headerView != null) {
                visible = visibleToAlpha(headerView.top, headerView.height)
                logd("top:${headerView.top}, h:${headerView.height}, visible:$visible", "")
            }
        }
        return visible
    }

    fun calcHeaderVisible(layoutManager: LinearLayoutManager) {
        viewModelScope.launch(Dispatchers.Default) {
            headerVisibleLiveData.postValue(normalizeHeaderVisible(layoutManager))
        }
    }

    private fun normalizeHeaderVisible(headerView: View, barHeight: Int): Float {
        val currentRect = Rect()
        val isVisible = headerView.getGlobalVisibleRect(currentRect)
        val height = headerView.height
        val currentHeight = if (isVisible) currentRect.height() else 0
        return heightToAlpha(currentHeight - barHeight, height - barHeight)
    }

    private fun heightToAlpha(top: Int, height: Int): Float{
        return max(0f, top.toFloat() / height)
    }

    fun calcHeaderVisible(headerView: View, barHeight: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            headerVisibleLiveData.postValue(1 - normalizeHeaderVisible(headerView, barHeight))
        }
    }
}

