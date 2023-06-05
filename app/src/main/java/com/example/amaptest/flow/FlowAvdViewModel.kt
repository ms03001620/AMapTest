package com.example.amaptest.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FlowAvdViewModel : ViewModel() {
    private val newsApi = AvdRepository()

    val uuid = combine(newsApi.randomIndexFlow, newsApi.randomUuidFlow) { index, str ->
        val sb = StringBuilder(str)
        sb.insert(index, "[")
        sb.insert(index + 2, "]")
        sb.toString()
    }

    fun update() {
        viewModelScope.launch {
            newsApi.update()
        }
    }

}