package com.example.amaptest.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FlowAvdViewModel : ViewModel() {
    private val newsApi = AvdRepository()

    val uuid = combine(newsApi.indexFlow, newsApi.uuidFlow) { index, uuid ->
        val sb = StringBuilder(uuid)
        sb.insert(index, "[")
        sb.insert(index + 2, "]")
        sb.toString()
    }

    fun createUuid() {
        viewModelScope.launch {
            newsApi.createUuid()
        }
    }

}