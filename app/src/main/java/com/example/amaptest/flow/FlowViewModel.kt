package com.example.amaptest.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class FlowViewModel : ViewModel() {
    private val newsApi = NewsRepository()
    val news = MutableLiveData<Int>()

    fun getNews() {
        viewModelScope.launch {
            newsApi.latestNews.collect {
                news.value = it
            }
        }
    }

}